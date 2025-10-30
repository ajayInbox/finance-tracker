class SmsMessageObject {

  final String address;
  final String messageHeader;
  final String messageBody;
  final String messageDate;

  SmsMessageObject({required this.address, required this.messageHeader, required this.messageBody, required this.messageDate});

  Map<String, dynamic> toJson () {
    return {
      'messageAddress': address,
      'messageHeader': messageHeader,
      'messageBody': messageBody,
      'messageDate': messageDate 
    };
  }

  
}