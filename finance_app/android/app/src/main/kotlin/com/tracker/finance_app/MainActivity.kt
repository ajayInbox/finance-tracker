package com.tracker.finance_app

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(engine: FlutterEngine) {
        super.configureFlutterEngine(engine)

        TransactionStore.init(this)

        MethodChannel(
            engine.dartExecutor.binaryMessenger,
            "transaction_channel"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "getTransactions" -> result.success(TransactionStore.getAll())
                else -> result.notImplemented()
            }
        }

        // ➕ NEW EventChannel
        EventChannel(
            engine.dartExecutor.binaryMessenger,
            "parsed_txn_events"
        ).setStreamHandler(object : EventChannel.StreamHandler {

            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                ParsedTxnEventEmitter.setSink(events)
            }

            override fun onCancel(arguments: Any?) {
                ParsedTxnEventEmitter.setSink(null)
            }
        })
    }
}
