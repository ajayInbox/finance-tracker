package com.tracker.finance_app

import io.flutter.plugin.common.EventChannel

/**
 * Singleton emitter that pushes sync-status updates to Dart via an EventChannel.
 *
 * Status strings emitted:
 *   • "SYNCING"
 *   • "SUCCESS:<count>:<epochMillis>"
 *   • "ERROR:<message>"
 */
object SyncStatusEmitter {

    private var eventSink: EventChannel.EventSink? = null

    fun setSink(sink: EventChannel.EventSink?) {
        eventSink = sink
    }

    fun emit(status: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            eventSink?.success(status)
        }
    }
}
