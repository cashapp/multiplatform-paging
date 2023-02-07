package app.cash.paging

import android.util.Log

actual fun platformLogger(): Logger? = object : Logger {

  override fun isLoggable(level: Int): Boolean = Log.isLoggable(LOG_TAG, level)

  override fun log(level: Int, message: String, tr: Throwable?) {
    when (level) {
      Log.DEBUG -> Log.d(LOG_TAG, message, tr)
      Log.VERBOSE -> Log.v(LOG_TAG, message, tr)
      else -> {
        throw IllegalArgumentException(
          "debug level $level is requested but Paging only supports " +
            "default logging for level 2 (DEBUG) or level 3 (VERBOSE)",
        )
      }
    }
  }
}
