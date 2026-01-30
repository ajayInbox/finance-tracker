import 'package:flutter/services.dart';

class ParsedTxnChannel {

  static const EventChannel _channel =
      EventChannel('parsed_txn_events');

  static Stream<String> stream() {
    return _channel.receiveBroadcastStream().cast<String>();
  }
  
}