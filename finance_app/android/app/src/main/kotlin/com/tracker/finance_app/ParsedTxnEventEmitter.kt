package com.tracker.finance_app

import io.flutter.plugin.common.EventChannel

object ParsedTxnEventEmitter {

    private var eventSink: EventChannel.EventSink? = null

    fun setSink(sink: EventChannel.EventSink?) {
        eventSink = sink
    }

    fun emit(uniqueIdentifier: String) {
        // Use a Handler to post to the Main Looper
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            eventSink?.success(uniqueIdentifier)
        }
    }
}