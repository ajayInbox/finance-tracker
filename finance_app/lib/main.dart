// lib/main.dart
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:another_telephony/telephony.dart';
import 'package:finance_app/data/services/account_service.dart';
import 'package:finance_app/data/services/category_service.dart';
import 'package:finance_app/data/services/transaction_service.dart';
import 'package:finance_app/utils/message_parser.dart';
import 'package:finance_app/data/models/transaction.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:finance_app/pages/transactions_page.dart';
import 'package:finance_app/pages/dashboard_page.dart';
import 'package:finance_app/pages/accounts_page.dart';
import 'package:finance_app/pages/settings_page.dart';

Future<void> _onBackgroundSmsReceived(SmsMessage message) async {
  // Handle incoming SMS in background
  // Parse and add transaction similar to SmsTransactionSyncService
  final parsed = MessageParser().parse(message.body ?? '');
  if (parsed.isValid) {
    final accounts = await AccountService().getAccounts();
    final categories = await CategoryService().getAllCategories();
    if (accounts.isEmpty || categories.isEmpty) return;

    String accountId = accounts.first.id;
    String categoryId = categories.first.id;
    if (parsed.categoryHint != null) {
      final matchedCat = categories.firstWhere(
        (c) => c.label.toLowerCase() == parsed.categoryHint!.toLowerCase(),
        orElse: () => categories.first,
      );
      categoryId = matchedCat.id;
    }

    final transaction = Transaction(
      transactionName: '${parsed.merchant ?? 'SMS Transaction'} Transaction',
      amount: parsed.amount!,
      type: 'Expense',
      account: accountId,
      category: categoryId,
      occuredAt: parsed.date ?? DateTime.now(),
      notes: 'Auto-added from SMS: ${message.body ?? ''}',
    );
    await TransactionService().addTransaction(transaction);
  }
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Load SMS listening preference
  final prefs = await SharedPreferences.getInstance();
  final smsListeningEnabled = prefs.getBool('sms_listening_enabled') ?? false;

  // Only start listening for SMS if user has enabled it
  if (smsListeningEnabled) {
    Telephony.instance.listenIncomingSms(
      onNewMessage: _onBackgroundSmsReceived,
    );
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Finance Tracker',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: MyHomePage(title: "Finance Tracker"),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _selectedIndex = 0;

  static final List<Widget> _widgetOptions = <Widget>[
    DashboardPage(),
    TransactionsPage(),
    AccountsPage(),
  ];

  @override
  void initState() {
    super.initState();
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _buildAppBar(),
      bottomNavigationBar: _buildBottomNavigationBar(),
      body: _widgetOptions[_selectedIndex],
      drawer: _buildDrawer(),
    );
  }

  AppBar _buildAppBar() {
    return AppBar(
      elevation: 0,
      backgroundColor: Colors.white,
      foregroundColor: Colors.black87,
      title: Text(
        widget.title,
        style: GoogleFonts.lato(
          textStyle: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
      ),
      leading: Builder(
        builder: (context) {
          return Container(
            margin: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.blue.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: IconButton(
              icon: const Icon(Icons.menu, color: Colors.blue),
              onPressed: () {
                Scaffold.of(context).openDrawer();
              },
            ),
          );
        },
      ),
      actions: [
        _buildNotificationButton(),
      ],
    );
  }

  Widget _buildNotificationButton() {
    // Notification Icon with Badge
              return Container(
                margin: const EdgeInsets.only(right: 8),
                padding: EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.grey.withOpacity(0.1),
                      blurRadius: 8,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Stack(
                  children: [
                    const Icon(
                      Icons.notifications_outlined,
                      color: Color(0xFF4A90E2),
                      size: 24,
                    ),
                    Positioned(
                      top: 0,
                      right: 0,
                      child: Container(
                        width: 8,
                        height: 8,
                        decoration: const BoxDecoration(
                          color: Colors.red,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                  ],
                ),
              );
  }

  Widget _buildBottomNavigationBar() {
    return NavigationBar(
      selectedIndex: _selectedIndex,
      onDestinationSelected: (i) => _onItemTapped(i),
      backgroundColor: Colors.white,
      elevation: 8,
      destinations: [
        NavigationDestination(
          icon: Icon(
            _selectedIndex == 0 ? Icons.home : Icons.home_outlined,
            color: _selectedIndex == 0 ? Colors.blue : Colors.grey,
          ),
          label: 'Home',
        ),
        NavigationDestination(
          icon: Icon(
            _selectedIndex == 1 ? Icons.list_alt : Icons.list_alt_outlined,
            color: _selectedIndex == 1 ? Colors.blue : Colors.grey,
          ),
          label: 'Transactions',
        ),
        NavigationDestination(
          icon: Icon(
            _selectedIndex == 2 ? Icons.account_balance : Icons.account_balance_outlined,
            color: _selectedIndex == 2 ? Colors.blue : Colors.grey,
          ),
          label: 'Accounts',
        ),
      ],
    );
  }

  Widget _buildDrawer() {
    return Drawer(
      child: ListView(
        padding: EdgeInsets.zero,
        children: [
          _buildDrawerHeader(),
          const Divider(),
          _buildDrawerItem(
            icon: Icons.home,
            title: 'Home',
            isSelected: _selectedIndex == 0,
            onTap: () {
              _onItemTapped(0);
              Navigator.pop(context);
            },
          ),
          _buildDrawerItem(
            icon: Icons.list_alt,
            title: 'Transactions',
            isSelected: _selectedIndex == 1,
            onTap: () {
              _onItemTapped(1);
              Navigator.pop(context);
            },
          ),
          _buildDrawerItem(
            icon: Icons.account_balance,
            title: 'Accounts',
            isSelected: _selectedIndex == 2,
            onTap: () {
              _onItemTapped(2);
              Navigator.pop(context);
            },
          ),
          const Divider(),
          _buildDrawerItem(
            icon: Icons.settings,
            title: 'Settings',
            isSelected: false,
            onTap: () {
              Navigator.pop(context); // Close drawer
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
              );
            },
          ),
          _buildDrawerItem(
            icon: Icons.help_outline,
            title: 'Help & Support',
            isSelected: false,
            onTap: () {
              // TODO: Navigate to help
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Help coming soon!')),
              );
            },
          ),
          _buildDrawerItem(
            icon: Icons.info_outline,
            title: 'About',
            isSelected: false,
            onTap: () {
              // TODO: Show about dialog
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('About coming soon!')),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildDrawerHeader() {
    final now = DateTime.now();
    final hour = now.hour;
    String timeOfDay;

    if (hour < 12) {
      timeOfDay = 'Morning';
    } else if (hour < 17) {
      timeOfDay = 'Afternoon';
    } else {
      timeOfDay = 'Evening';
    }

    return Container(
      padding: const EdgeInsets.fromLTRB(16, 48, 16, 16),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.blue, Colors.blueAccent],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const CircleAvatar(
            radius: 30,
            backgroundColor: Colors.white,
            child: Text(
              'U',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Colors.blue,
              ),
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'Good $timeOfDay!',
            style: GoogleFonts.lato(
              textStyle: const TextStyle(
                color: Colors.white70,
                fontSize: 14,
              ),
            ),
          ),
          Text(
            'User',
            style: GoogleFonts.lato(
              textStyle: const TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDrawerItem({
    required IconData icon,
    required String title,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return ListTile(
      leading: Icon(
        icon,
        color: isSelected ? Colors.blue : Colors.grey[600],
      ),
      title: Text(
        title,
        style: TextStyle(
          color: isSelected ? Colors.blue : Colors.black87,
          fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
        ),
      ),
      selected: isSelected,
      selectedTileColor: Colors.blue.withOpacity(0.1),
      onTap: onTap,
    );
  }
}
