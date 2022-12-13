/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.video

import android.Manifest
import android.content.Context
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.pipe.integration.CameraPipeConfig
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.impl.utils.AspectRatioUtil
import androidx.camera.core.impl.utils.TransformUtils.is90or270
import androidx.camera.core.impl.utils.TransformUtils.rectToSize
import androidx.camera.core.impl.utils.TransformUtils.rotateSize
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.testing.CameraPipeConfigTestRule
import androidx.camera.testing.CameraUtil
import androidx.camera.testing.SurfaceTextureProvider
import androidx.camera.testing.fakes.FakeLifecycleOwner
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_NONE
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE
import androidx.core.util.Consumer
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

@LargeTest
@RunWith(Parameterized::class)
@SdkSuppress(minSdkVersion = 21)
class VideoRecordingTest(
    private val implName: String,
    private var cameraSelector: CameraSelector,
    private val cameraConfig: CameraXConfig
) {

    @get:Rule
    val cameraPipeConfigTestRule = CameraPipeConfigTestRule(
        active = implName.contains(CameraPipeConfig::class.simpleName!!),
    )

    @get:Rule
    val cameraRule = CameraUtil.grantCameraPermissionAndPreTest(
        CameraUtil.PreTestCameraIdList(cameraConfig)
    )

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    companion object {
        private const val VIDEO_TIMEOUT_SEC = 10L
        private const val TAG = "VideoRecordingTest"

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "back+" + Camera2Config::class.simpleName,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    Camera2Config.defaultConfig()
                ),
                arrayOf(
                    "front+" + Camera2Config::class.simpleName,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    Camera2Config.defaultConfig()
                ),
                arrayOf(
                    "back+" + CameraPipeConfig::class.simpleName,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    CameraPipeConfig.defaultConfig()
                ),
                arrayOf(
                    "front+" + CameraPipeConfig::class.simpleName,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    CameraPipeConfig.defaultConfig()
                ),
            )
        }
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var lifecycleOwner: FakeLifecycleOwner
    private lateinit var preview: Preview
    private lateinit var cameraInfo: CameraInfo
    private lateinit var camera: Camera

    private lateinit var latchForVideoSaved: CountDownLatch
    private lateinit var latchForVideoRecording: CountDownLatch

    private lateinit var finalize: VideoRecordEvent.Finalize

    private val audioSourceAvailable by lazy {
        AudioChecker.canAudioSourceBeStarted(
            context, cameraSelector, Recorder.DEFAULT_QUALITY_SELECTOR
        )
    }

    private val videoRecordEventListener = Consumer<VideoRecordEvent> {
        when (it) {
            is VideoRecordEvent.Start -> {
                // Recording start.
                Log.d(TAG, "Recording start")
            }
            is VideoRecordEvent.Finalize -> {
                // Recording stop.
                Log.d(TAG, "Recording finalize")
                finalize = it
                latchForVideoSaved.countDown()
            }
            is VideoRecordEvent.Status -> {
                // Make sure the recording proceed for a while.
                latchForVideoRecording.countDown()
            }
            is VideoRecordEvent.Pause, is VideoRecordEvent.Resume -> {
                // no op for this test, skip these event now.
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    @Before
    fun setUp() {
        assumeTrue(CameraUtil.hasCameraWithLensFacing(cameraSelector.lensFacing!!))
        // Skip for b/168175357, b/233661493
        assumeFalse(
            "Skip tests for Cuttlefish MediaCodec issues",
            Build.MODEL.contains("Cuttlefish") &&
                (Build.VERSION.SDK_INT == 29 || Build.VERSION.SDK_INT == 33)
        )

        ProcessCameraProvider.configureInstance(cameraConfig)
        cameraProvider = ProcessCameraProvider.getInstance(context).get()
        lifecycleOwner = FakeLifecycleOwner()
        lifecycleOwner.startAndResume()

        // Add extra Preview to provide an additional surface for b/168187087.
        preview = Preview.Builder().build()

        instrumentation.runOnMainSync {
            // Sets surface provider to preview
            preview.setSurfaceProvider(getSurfaceProvider())

            // Retrieves the target testing camera and camera info
            camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector)
            cameraInfo = camera.cameraInfo
        }
    }

    @After
    fun tearDown() {
        if (this::cameraProvider.isInitialized) {
            instrumentation.runOnMainSync {
                cameraProvider.unbindAll()
            }
            cameraProvider.shutdown()[10, TimeUnit.SECONDS]
        }
    }

    @Test
    fun getMetadataRotation_when_setTargetRotation() {
        // Arrange.
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        // Just set one Surface.ROTATION_90 to verify the function work or not.
        val targetRotation = Surface.ROTATION_90
        videoCapture.targetRotation = targetRotation

        val file = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        latchForVideoSaved = CountDownLatch(1)
        latchForVideoRecording = CountDownLatch(5)

        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }

        // Act.
        completeVideoRecording(videoCapture, file)

        // Verify.
        verifyMetadataRotation(getExpectedRotation(videoCapture).metadataRotation, file)

        // Cleanup.
        file.delete()
    }

    @Test
    fun getCorrectResolution_when_setSupportedQuality() {
        // Pre-arrange.
        assumeTrue(QualitySelector.getSupportedQualities(cameraInfo).isNotEmpty())
        val qualityList = QualitySelector.getSupportedQualities(cameraInfo)
        Log.d(TAG, "CameraSelector: ${cameraSelector.lensFacing}, QualityList: $qualityList ")

        qualityList.forEach loop@{ quality ->
            // Arrange.
            val targetResolution = QualitySelector.getResolution(cameraInfo, quality)
            if (targetResolution == null) {
                // If targetResolution is null, try next one
                Log.e(TAG, "Unable to get resolution for the quality: $quality")
                return@loop
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality)).build()

            val videoCapture = VideoCapture.withOutput(recorder)

            if (!camera.isUseCasesCombinationSupported(preview, videoCapture)) {
                Log.e(TAG, "The UseCase combination is not supported for quality setting: $quality")
                return@loop
            }

            instrumentation.runOnMainSync {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            }

            val file = File.createTempFile("video_$targetResolution", ".tmp")
                .apply { deleteOnExit() }

            latchForVideoSaved = CountDownLatch(1)
            latchForVideoRecording = CountDownLatch(5)

            // Act.
            completeVideoRecording(videoCapture, file)

            // Verify.
            verifyVideoResolution(getExpectedResolution(videoCapture), file)

            // Cleanup.
            file.delete()
        }
    }

    @Test
    fun getCorrectResolution_when_setAspectRatio() {
        // Pre-arrange.
        assumeTrue(QualitySelector.getSupportedQualities(cameraInfo).isNotEmpty())

        for (aspectRatio in listOf(AspectRatio.RATIO_4_3, AspectRatio.RATIO_16_9)) {
            // Arrange.
            val recorder = Recorder.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            val videoCapture = VideoCapture.withOutput(recorder)

            if (!camera.isUseCasesCombinationSupported(preview, videoCapture)) {
                continue
            }

            instrumentation.runOnMainSync {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            }

            val file = File.createTempFile("video_", ".tmp").apply { deleteOnExit() }

            latchForVideoSaved = CountDownLatch(1)
            latchForVideoRecording = CountDownLatch(5)

            // Act.
            completeVideoRecording(videoCapture, file)

            // Verify.
            verifyVideoAspectRatio(getExpectedAspectRatio(videoCapture)!!, file)

            // Cleanup.
            file.delete()
        }
    }

    @Test
    fun getCorrectResolution_when_setCropRect() {
        assumeSuccessfulSurfaceProcessing()

        // Arrange.
        assumeTrue(QualitySelector.getSupportedQualities(cameraInfo).isNotEmpty())
        val quality = Quality.LOWEST
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(quality)).build()
        val videoCapture = VideoCapture.withOutput(recorder)
        // Arbitrary cropping
        val targetResolution = QualitySelector.getResolution(cameraInfo, quality)!!
        val cropRect = Rect(6, 6, targetResolution.width - 7, targetResolution.height - 7)
        videoCapture.setViewPortCropRect(cropRect)

        assumeTrue(
            "The UseCase combination is not supported for quality setting: $quality",
            camera.isUseCasesCombinationSupported(preview, videoCapture)
        )

        instrumentation.runOnMainSync {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
        }

        val file = File.createTempFile("video_", ".tmp").apply { deleteOnExit() }

        latchForVideoSaved = CountDownLatch(1)
        latchForVideoRecording = CountDownLatch(5)

        // Act.
        completeVideoRecording(videoCapture, file)

        // Verify.
        verifyVideoResolution(getExpectedResolution(videoCapture), file)

        // Cleanup.
        file.delete()
    }

    @Test
    fun stopRecording_when_useCaseUnbind() {
        // Arrange.
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        val file = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        latchForVideoSaved = CountDownLatch(1)
        latchForVideoRecording = CountDownLatch(5)

        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }

        // Act.
        startVideoRecording(videoCapture, file).use {
            instrumentation.runOnMainSync {
                cameraProvider.unbind(videoCapture)
            }

            // Verify.
            // Wait for finalize event to saved file.
            assertThat(latchForVideoSaved.await(VIDEO_TIMEOUT_SEC, TimeUnit.SECONDS)).isTrue()

            assertThat(finalize.error).isEqualTo(ERROR_SOURCE_INACTIVE)

            // Cleanup.
            file.delete()
        }
    }

    @Test
    fun stopRecordingWhenLifecycleStops() {
        // Arrange.
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        val file = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        latchForVideoSaved = CountDownLatch(1)
        latchForVideoRecording = CountDownLatch(5)

        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }

        // Act.
        startVideoRecording(videoCapture, file).use {
            instrumentation.runOnMainSync {
                lifecycleOwner.pauseAndStop()
            }

            // Verify.
            // Wait for finalize event to saved file.
            assertThat(latchForVideoSaved.await(VIDEO_TIMEOUT_SEC, TimeUnit.SECONDS)).isTrue()

            assertThat(finalize.error).isEqualTo(ERROR_SOURCE_INACTIVE)

            // Cleanup.
            file.delete()
        }
    }

    @Test
    fun start_finalizeImmediatelyWhenSourceInactive() {
        // Arrange.
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        val file = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }

        @Suppress("UNCHECKED_CAST")
        val mockListener = mock(Consumer::class.java) as Consumer<VideoRecordEvent>
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
            lifecycleOwner.pauseAndStop()
        }

        // Act.
        videoCapture.output
            .prepareRecording(context, FileOutputOptions.Builder(file).build())
            .start(CameraXExecutors.directExecutor(), mockListener).use {

                // Verify.
                verify(mockListener, timeout(5000L))
                    .accept(any(VideoRecordEvent.Finalize::class.java))
                verifyNoMoreInteractions(mockListener)
                val captor = ArgumentCaptor.forClass(VideoRecordEvent::class.java)
                verify(mockListener, atLeastOnce()).accept(captor.capture())
                val finalize = captor.value as VideoRecordEvent.Finalize
                assertThat(finalize.error).isEqualTo(ERROR_SOURCE_INACTIVE)

                // Cleanup.
                file.delete()
            }
    }

    @Test
    fun recordingWithPreviewAndImageAnalysis() {
        // Pre-check and arrange
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        val analysis = ImageAnalysis.Builder().build()
        assumeTrue(camera.isUseCasesCombinationSupported(preview, videoCapture, analysis))

        val file = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        latchForVideoSaved = CountDownLatch(1)
        latchForVideoRecording = CountDownLatch(5)
        val latchForImageAnalysis = CountDownLatch(5)
        analysis.setAnalyzer(CameraXExecutors.directExecutor()) {
            latchForImageAnalysis.countDown()
            it.close()
        }

        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                analysis,
                videoCapture
            )
        }

        // Act.
        completeVideoRecording(videoCapture, file)

        // Verify.
        verifyRecordingResult(file)
        assertThat(latchForImageAnalysis.await(10, TimeUnit.SECONDS)).isTrue()
        // Cleanup.
        file.delete()
    }

    @Test
    fun recordingWithPreviewAndImageCapture() {
        // Pre-check and arrange
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        val imageCapture = ImageCapture.Builder().build()
        assumeTrue(
            camera.isUseCasesCombinationSupported(
                preview,
                videoCapture,
                imageCapture
            )
        )

        val videoFile = File.createTempFile("camerax-video", ".tmp").apply {
            deleteOnExit()
        }
        val imageFile = File.createTempFile("camerax-image-capture", ".tmp").apply {
            deleteOnExit()
        }
        latchForVideoSaved = CountDownLatch(1)
        latchForVideoRecording = CountDownLatch(5)

        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
        }

        // Act.
        completeVideoRecording(videoCapture, videoFile)
        completeImageCapture(imageCapture, imageFile)

        // Verify.
        verifyRecordingResult(videoFile)

        // Cleanup.
        videoFile.delete()
        imageFile.delete()
    }

    @Test
    fun canRecordMultipleFilesInARow() {
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }
        val file1 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file1, includeAudio = audioSourceAvailable)

        val file2 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file2, includeAudio = audioSourceAvailable)

        val file3 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file3, includeAudio = audioSourceAvailable)

        verifyRecordingResult(file1, audioSourceAvailable)
        verifyRecordingResult(file2, audioSourceAvailable)
        verifyRecordingResult(file3, audioSourceAvailable)

        file1.delete()
        file2.delete()
        file3.delete()
    }

    @Test
    fun canRecordMultipleFilesWithThenWithoutAudio() {
        // This test requires that audio is available
        assumeTrue(audioSourceAvailable)
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }
        val file1 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file1, includeAudio = true)

        val file2 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file2, includeAudio = false)

        verifyRecordingResult(file1, true)
        verifyRecordingResult(file2, false)

        file1.delete()
        file2.delete()
    }

    @Test
    fun canRecordMultipleFilesWithoutThenWithAudio() {
        // This test requires that audio is available
        assumeTrue(audioSourceAvailable)
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }
        val file1 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file1, includeAudio = false)

        val file2 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        performRecording(videoCapture, file2, includeAudio = true)

        verifyRecordingResult(file1, false)
        verifyRecordingResult(file2, true)

        file1.delete()
        file2.delete()
    }

    @Test
    fun canStartNextRecordingPausedAfterFirstRecordingFinalized() {
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }

        // Start and stop a recording to ensure recorder is idling
        val file1 = File.createTempFile("CameraX1", ".tmp").apply { deleteOnExit() }

        performRecording(videoCapture, file1, audioSourceAvailable)

        // First recording is now finalized. Try starting second recording paused.
        @Suppress("UNCHECKED_CAST")
        val mockListener = mock(Consumer::class.java) as Consumer<VideoRecordEvent>
        val inOrder = inOrder(mockListener)
        val file2 = File.createTempFile("CameraX2", ".tmp").apply { deleteOnExit() }
        videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file2).build())
            .apply {
                if (audioSourceAvailable) {
                    withAudioEnabled()
                }
            }.start(CameraXExecutors.directExecutor(), mockListener).use { activeRecording2 ->

                activeRecording2.pause()

                inOrder.verify(mockListener, timeout(5000L))
                    .accept(any(VideoRecordEvent.Start::class.java))

                inOrder.verify(mockListener, timeout(5000L))
                    .accept(any(VideoRecordEvent.Pause::class.java))

                activeRecording2.stop()
            }

        file1.delete()
        file2.delete()
    }

    @Test
    fun nextRecordingCanBeStartedAfterLastRecordingStopped() {
        @Suppress("UNCHECKED_CAST")
        val mockListener = mock(Consumer::class.java) as Consumer<VideoRecordEvent>
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }
        val file1 = File.createTempFile("CameraX1", ".tmp").apply { deleteOnExit() }
        val file2 = File.createTempFile("CameraX2", ".tmp").apply { deleteOnExit() }

        val inOrder = inOrder(mockListener)
        try {
            videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file1).build())
                .start(CameraXExecutors.directExecutor(), mockListener).use {
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Start::class.java))
                    inOrder.verify(mockListener, timeout(15000L).atLeast(5))
                        .accept(any(VideoRecordEvent.Status::class.java))
                }

            videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file2).build())
                .start(CameraXExecutors.directExecutor(), mockListener).use {
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Finalize::class.java))
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Start::class.java))
                    inOrder.verify(mockListener, timeout(15000L).atLeast(5))
                        .accept(any(VideoRecordEvent.Status::class.java))
                }

            inOrder.verify(mockListener, timeout(5000L))
                .accept(any(VideoRecordEvent.Finalize::class.java))

            verifyRecordingResult(file1)
            verifyRecordingResult(file2)
        } finally {
            file1.delete()
            file2.delete()
        }
    }

    @Test
    fun canSwitchAudioOnOff() {
        assumeTrue("Audio source is not available", audioSourceAvailable)
        @Suppress("UNCHECKED_CAST")
        val mockListener = mock(Consumer::class.java) as Consumer<VideoRecordEvent>
        val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
        instrumentation.runOnMainSync {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        }

        val file1 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        val file2 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }
        val file3 = File.createTempFile("CameraX", ".tmp").apply { deleteOnExit() }

        val inOrder = inOrder(mockListener)
        try {
            // Record the first video with audio enabled.
            videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file1).build())
                .withAudioEnabled()
                .start(CameraXExecutors.directExecutor(), mockListener).use {
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Start::class.java))
                    inOrder.verify(mockListener, timeout(15000L).atLeast(5))
                        .accept(any(VideoRecordEvent.Status::class.java))
                }

            // Record the second video with audio disabled.
            videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file2).build())
                .start(CameraXExecutors.directExecutor(), mockListener).use {
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Finalize::class.java))
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Start::class.java))
                    inOrder.verify(mockListener, timeout(15000L).atLeast(5))
                        .accept(any(VideoRecordEvent.Status::class.java))

                    // Check the audio information reports state as disabled.
                    val captor = ArgumentCaptor.forClass(VideoRecordEvent::class.java)
                    verify(mockListener, atLeastOnce()).accept(captor.capture())
                    assertThat(captor.value).isInstanceOf(VideoRecordEvent.Status::class.java)
                    val status = captor.value as VideoRecordEvent.Status
                    assertThat(status.recordingStats.audioStats.audioState)
                        .isEqualTo(AudioStats.AUDIO_STATE_DISABLED)
                }

            // Record the third video with audio enabled.
            videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file3).build())
                .withAudioEnabled()
                .start(CameraXExecutors.directExecutor(), mockListener).use {
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Finalize::class.java))
                    inOrder.verify(mockListener, timeout(5000L))
                        .accept(any(VideoRecordEvent.Start::class.java))
                    inOrder.verify(mockListener, timeout(15000L).atLeast(5))
                        .accept(any(VideoRecordEvent.Status::class.java))
                }

            inOrder.verify(mockListener, timeout(5000L))
                .accept(any(VideoRecordEvent.Finalize::class.java))

            // Check the audio in file is as expected.
            verifyRecordingResult(file1, true)
            verifyRecordingResult(file2, false)
            verifyRecordingResult(file3, true)
        } finally {
            file1.delete()
            file2.delete()
            file3.delete()
        }
    }

    private fun performRecording(
        videoCapture: VideoCapture<Recorder>,
        file: File,
        includeAudio: Boolean = false
    ) {
        @Suppress("UNCHECKED_CAST")
        val mockListener = mock(Consumer::class.java) as Consumer<VideoRecordEvent>
        val inOrder = inOrder(mockListener)
        videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file).build())
            .apply {
                if (includeAudio) {
                    withAudioEnabled()
                }
            }
            .start(CameraXExecutors.directExecutor(), mockListener).use { activeRecording ->

                inOrder.verify(mockListener, timeout(5000L))
                    .accept(any(VideoRecordEvent.Start::class.java))
                inOrder.verify(mockListener, timeout(15000L).atLeast(5))
                    .accept(any(VideoRecordEvent.Status::class.java))

                activeRecording.stop()
            }

        inOrder.verify(mockListener, timeout(5000L))
            .accept(any(VideoRecordEvent.Finalize::class.java))

        val captor = ArgumentCaptor.forClass(VideoRecordEvent::class.java)
        verify(mockListener, atLeastOnce()).accept(captor.capture())

        val finalizeEvent = captor.allValues.last() as VideoRecordEvent.Finalize

        assertRecordingSuccessful(finalizeEvent, checkAudio = includeAudio)
    }

    private fun assertRecordingSuccessful(
        finalizeEvent: VideoRecordEvent.Finalize,
        checkAudio: Boolean = false
    ) {
        assertWithMessage(
            "Recording did not finish successfully. Finished with error: ${
                VideoRecordEvent.Finalize.errorToString(
                    finalizeEvent.error
                )
            }"
        ).that(finalizeEvent.error).isEqualTo(ERROR_NONE)
        if (checkAudio) {
            val audioStats = finalizeEvent.recordingStats.audioStats
            assertWithMessage(
                "Recording with audio encountered audio error." +
                    "\n${audioStats.errorCause?.stackTraceToString()}"
            ).that(audioStats.audioState).isNotEqualTo(AudioStats.AUDIO_STATE_ENCODER_ERROR)
        }
    }

    private fun startVideoRecording(videoCapture: VideoCapture<Recorder>, file: File):
        Recording {
        val recording = videoCapture.output
            .prepareRecording(context, FileOutputOptions.Builder(file).build())
            .start(CameraXExecutors.directExecutor(), videoRecordEventListener)

        try {
            // Wait for status event to proceed recording for a while.
            assertThat(latchForVideoRecording.await(VIDEO_TIMEOUT_SEC, TimeUnit.SECONDS))
                .isTrue()
        } catch (ex: Exception) {
            recording.stop()
            throw ex
        }

        return recording
    }

    private fun completeVideoRecording(videoCapture: VideoCapture<Recorder>, file: File) {
        val recording = startVideoRecording(videoCapture, file)

        recording.stop()
        // Wait for finalize event to saved file.
        assertThat(latchForVideoSaved.await(VIDEO_TIMEOUT_SEC, TimeUnit.SECONDS)).isTrue()

        // Check if any error after recording finalized
        assertWithMessage(TAG + "Finalize with error: ${finalize.error}, ${finalize.cause}.")
            .that(finalize.hasError()).isFalse()
    }

    private fun completeImageCapture(imageCapture: ImageCapture, imageFile: File) {
        val savedCallback = ImageSavedCallback()

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(imageFile).build(),
            CameraXExecutors.ioExecutor(),
            savedCallback
        )
        savedCallback.verifyCaptureResult()
    }

    data class ExpectedRotation(val contentRotation: Int, val metadataRotation: Int)

    private fun getExpectedRotation(videoCapture: VideoCapture<Recorder>): ExpectedRotation {
        val rotationNeeded = cameraInfo.getSensorRotationDegrees(videoCapture.targetRotation)
        return if (videoCapture.node != null) {
            ExpectedRotation(rotationNeeded, 0)
        } else {
            ExpectedRotation(0, rotationNeeded)
        }
    }

    private fun getExpectedResolution(videoCapture: VideoCapture<Recorder>): Size =
        rotateSize(
            rectToSize(videoCapture.cropRect!!),
            getExpectedRotation(videoCapture).contentRotation
        )

    private fun getExpectedAspectRatio(videoCapture: VideoCapture<Recorder>): Rational? {
        val needRotate by lazy { is90or270(getExpectedRotation(videoCapture).contentRotation) }
        return when (videoCapture.output.aspectRatio) {
            AspectRatio.RATIO_4_3 ->
                if (needRotate) AspectRatioUtil.ASPECT_RATIO_3_4
                else AspectRatioUtil.ASPECT_RATIO_4_3
            AspectRatio.RATIO_16_9 ->
                if (needRotate) AspectRatioUtil.ASPECT_RATIO_9_16
                else AspectRatioUtil.ASPECT_RATIO_16_9
            else -> null
        }
    }

    private fun verifyMetadataRotation(expectedRotation: Int, file: File) {
        MediaMetadataRetriever().useAndRelease {
            it.setDataSource(context, Uri.fromFile(file))
            val videoRotation =
                it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!.toInt()

            // Checks the rotation from video file's metadata is matched with the relative rotation.
            assertWithMessage(
                TAG + ", rotation test failure: " +
                    "videoRotation: $videoRotation" +
                    ", expectedRotation: $expectedRotation"
            ).that(videoRotation).isEqualTo(expectedRotation)
        }
    }

    private fun verifyVideoResolution(expectedResolution: Size, file: File) {
        MediaMetadataRetriever().useAndRelease {
            it.setDataSource(context, Uri.fromFile(file))
            val height = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
                .toInt()
            val width = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!
                .toInt()
            val resolution = Size(width, height)

            // Compare with the resolution of video and the targetResolution in QualitySelector
            assertWithMessage(
                TAG + ", verifyVideoResolution failure:" +
                    ", videoResolution: $resolution" +
                    ", expectedResolution: $expectedResolution"
            ).that(resolution).isEqualTo(expectedResolution)
        }
    }

    private fun verifyVideoAspectRatio(expectedAspectRatio: Rational, file: File) {
        MediaMetadataRetriever().useAndRelease {
            it.setDataSource(context, Uri.fromFile(file))
            val height = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
                .toInt()
            val width = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!
                .toInt()
            val aspectRatio = Rational(width, height)

            assertWithMessage(
                TAG + ", verifyVideoAspectRatio failure:" +
                    ", videoAspectRatio: $aspectRatio" +
                    ", expectedAspectRatio: $expectedAspectRatio"
            ).that(aspectRatio.toDouble()).isWithin(0.1).of(expectedAspectRatio.toDouble())
        }
    }

    private fun verifyRecordingResult(file: File, hasAudio: Boolean = false) {
        MediaMetadataRetriever().useAndRelease {
            it.setDataSource(context, Uri.fromFile(file))
            val video = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            val audio = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)

            assertThat(video).isEqualTo("yes")
            assertThat(audio).isEqualTo(if (hasAudio) "yes" else null)
        }
    }

    private fun getSurfaceProvider(): Preview.SurfaceProvider {
        return SurfaceTextureProvider.createSurfaceTextureProvider(
            object : SurfaceTextureProvider.SurfaceTextureCallback {
                override fun onSurfaceTextureReady(
                    surfaceTexture: SurfaceTexture,
                    resolution: Size
                ) {
                    // No-op
                }

                override fun onSafeToRelease(surfaceTexture: SurfaceTexture) {
                    surfaceTexture.release()
                }
            }
        )
    }

    /** Skips tests which will enable surface processing and encounter device specific issues. */
    private fun assumeSuccessfulSurfaceProcessing() {
        // Skip for b/253211491
        assumeFalse(
            "Skip tests for Cuttlefish API 30 eglCreateWindowSurface issue",
            Build.MODEL.contains("Cuttlefish") && Build.VERSION.SDK_INT == 30
        )
    }

    private class ImageSavedCallback :
        ImageCapture.OnImageSavedCallback {

        private val latch = CountDownLatch(1)
        val results = mutableListOf<ImageCapture.OutputFileResults>()
        val errors = mutableListOf<ImageCaptureException>()

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            results.add(outputFileResults)
            latch.countDown()
        }

        override fun onError(exception: ImageCaptureException) {
            errors.add(exception)
            Log.e(TAG, "OnImageSavedCallback.onError: ${exception.message}")
            latch.countDown()
        }

        fun verifyCaptureResult() {
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
        }
    }
}

private fun MediaMetadataRetriever.useAndRelease(block: (MediaMetadataRetriever) -> Unit) {
    try {
        block(this)
    } finally {
        release()
    }
}
