class SmsMessageObject {

  final String messageAddress;
  final String messageHeader;
  final String messageBody;
  final String messageDate;

  SmsMessageObject({required this.messageAddress, required this.messageHeader, required this.messageBody, required this.messageDate});

  Map<String, dynamic> toJson () {
    return {
      'messageAddress': messageAddress,
      'messageHeader': messageHeader,
      'messageBody': messageBody,
      'messageDate': messageDate 
    };
  }

  factory SmsMessageObject.fromJson(Map<String, dynamic> json) => SmsMessageObject(
    messageAddress: json['messageAddress'] as String,
    messageHeader: json['messageHeader'] as String,
    messageBody: json['messageBody'] as String,
    messageDate: json['messageDate'] as String
  );

}