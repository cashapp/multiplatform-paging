package app.cash.paging

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation // marks this annotation as *not* req to have actual on every platform
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class Parcelize()
