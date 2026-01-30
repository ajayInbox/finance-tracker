// import 'package:finance_app/features/transaction/data/model/sms_message.dart';
// import 'package:finance_app/features/transaction/providers/export_sms_provider.dart';
// import 'package:flutter/material.dart';
// import 'package:flutter_riverpod/flutter_riverpod.dart';
// import 'package:google_fonts/google_fonts.dart';
// import 'package:another_telephony/telephony.dart';
// import 'package:permission_handler/permission_handler.dart';

// class SMSModal extends ConsumerStatefulWidget {
//   const SMSModal({super.key});

//   @override
//   ConsumerState<SMSModal> createState() => _SMSModalState();
// }

// class _SMSModalState extends ConsumerState<SMSModal> {
//   final telephony = Telephony.instance;
//   String _selectedDuration = 'Last 1 Month';
//   List<SmsMessage> _messages = [];
//   final List<SmsMessage> _selectedMessages = [];
//   bool _isScanning = false;
//   bool _isExporting = false;

//   @override
//   void initState() {
//     super.initState();
//     _requestPermissions();
//   }

//   Future<void> _requestPermissions() async {
//     await Permission.sms.request();
//     await Permission.phone.request();
//     await telephony.requestSmsPermissions;
//   }

//   Future<void> _scanMessages() async {
//     setState(() {
//       _isScanning = true;
//       _messages.clear();
//       _selectedMessages.clear();
//     });

//     final now = DateTime.now();
//     final startDate = _selectedDuration == 'Last 3 Months'
//         ? now.subtract(Duration(days: 90))
//         : now.subtract(Duration(days: 30));

//     try {
//       final inbox = await telephony.getInboxSms(
//         filter: SmsFilter.where(SmsColumn.ADDRESS).like("%"),
//         sortOrder: [OrderBy(SmsColumn.DATE, sort: Sort.DESC)],
//       );

//       // Filter date + detect transaction keywords
//       // final transactionPattern = RegExp(
//       //     r"(credited|debited|UPI|spent|received|payment|withdrawn|rs\.|inr)",
//       //     caseSensitive: false);

//       final filtered = inbox.where((msg) {
//         final ts = msg.date;
//         if (ts == null) return false;
//         final dt = DateTime.fromMillisecondsSinceEpoch(ts);
//         if (dt.isBefore(startDate)) return false;

//         return msg.body != null;
//       }).toList();

//       setState(() {
//         _messages = filtered;
//         _isScanning = false;
//       });
//     } catch (e) {
//       setState(() => _isScanning = false);
//       if (mounted) {
//         ScaffoldMessenger.of(context).showSnackBar(
//           SnackBar(content: Text('Failed to scan messages: $e')),
//         );
//       }
//     }
//   }

//   void _toggleSelect(SmsMessage msg) {
//     setState(() {
//       _selectedMessages.contains(msg)
//           ? _selectedMessages.remove(msg)
//           : _selectedMessages.add(msg);
//     });
//   }

//   Future<void> _exportToBackend() async {
//     if (_selectedMessages.isEmpty || _isExporting) return;

//     setState(() => _isExporting = true);

//     try {

//       List<SmsMessageObject> messages = _selectedMessages.map((msg) => SmsMessageObject(
//         messageAddress: msg.address!,
//         messageHeader: msg.address!,
//         messageBody: msg.body!,
//         messageDate: DateTime.fromMillisecondsSinceEpoch(msg.date!).toString()
//       )).toList();

//       await ref.read(exportSmsProvider(messages).future);

//       if (mounted) {
//         ScaffoldMessenger.of(context).showSnackBar(
//           const SnackBar(content: Text('Export started...')),
//         );
//         Navigator.pop(context);
//       }
//     } catch (e) {
//       if (mounted) {
//         ScaffoldMessenger.of(context).showSnackBar(
//           SnackBar(content: Text('Export failed: $e')),
//         );
//       }
//     } finally {
//       if (mounted) {
//         setState(() => _isExporting = false);
//       }
//     }
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Container(
//       height: MediaQuery.of(context).size.height * 0.9,
//       padding: EdgeInsets.all(16),
//       child: Column(
//         crossAxisAlignment: CrossAxisAlignment.start,
//         children: [
//           Row(
//             mainAxisAlignment: MainAxisAlignment.spaceBetween,
//             children: [
//               Text("SMS Scanner",
//                   style: GoogleFonts.inter(fontSize: 20, fontWeight: FontWeight.w600)),
//               IconButton(onPressed: () => Navigator.pop(context), icon: Icon(Icons.close)),
//             ],
//           ),
//           SizedBox(height: 16),
//           Text("Duration:",
//               style: GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.w500)),
//           SizedBox(height: 8),
//           Row(
//             children: [
//               _durationButton("Last 1 Month"),
//               SizedBox(width: 8),
//               _durationButton("Last 3 Months"),
//             ],
//           ),
//           SizedBox(height: 16),

//           ElevatedButton(
//             onPressed: _isScanning ? null : _scanMessages,
//             style: ElevatedButton.styleFrom(minimumSize: Size(double.infinity, 48)),
//             child: _isScanning ? CircularProgressIndicator() : Text("Scan Messages"),
//           ),
//           SizedBox(height: 16),

//           Expanded(child: _buildMessageList()),
//         ],
//       ),
//     );
//   }

//   Widget _durationButton(String text) {
//     final selected = _selectedDuration == text;
//     return ElevatedButton(
//       onPressed: () => setState(() => _selectedDuration = text),
//       style: ElevatedButton.styleFrom(
//         backgroundColor: selected ? Colors.blue : Colors.grey.shade300,
//         foregroundColor: selected ? Colors.white : Colors.black,
//       ),
//       child: Text(text),
//     );
//   }

//   Widget _buildMessageList() {
//     if (_isScanning) return Center(child: CircularProgressIndicator());
//     if (_messages.isEmpty) {
//       return Center(child: Text("No transaction SMS found"));
//     }

//     return Column(
//       children: [
//         Row(
//           mainAxisAlignment: MainAxisAlignment.spaceBetween,
//           children: [
//             Text("Messages (${_messages.length})"),
//             if (_selectedMessages.isNotEmpty)
//               ElevatedButton(
//                   onPressed: _isExporting ? null : _exportToBackend,
//                   child: _isExporting
//                       ? SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
//                       : Text("Export (${_selectedMessages.length})"))
//           ],
//         ),
//         SizedBox(height: 8),
//         Expanded(
//           child: ListView.builder(
//             itemCount: _messages.length,
//             itemBuilder: (_, i) {
//               final msg = _messages[i];
//               final selected = _selectedMessages.contains(msg);
//               return ListTile(
//                 leading: Checkbox(value: selected, onChanged: (_) => _toggleSelect(msg)),
//                 title: Text(msg.body ?? "No Message"),
//                 subtitle: Text("From: ${msg.address ?? "Unknown"}"),
//               );
//             },
//           ),
//         ),
//       ],
//     );
//   }
// }
