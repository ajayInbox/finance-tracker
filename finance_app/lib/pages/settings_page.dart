import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:permission_handler/permission_handler.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool _smsListeningEnabled = false;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _smsListeningEnabled = prefs.getBool('sms_listening_enabled') ?? false;
      _isLoading = false;
    });
  }

  Future<void> _toggleSmsListening(bool value) async {
    final prefs = await SharedPreferences.getInstance();

    if (value) {
      // Check SMS permission before enabling
      final smsPermission = await Permission.sms.status;
      if (smsPermission.isDenied || smsPermission.isPermanentlyDenied) {
        final granted = await Permission.sms.request();
        if (!granted.isGranted) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('SMS permission is required to enable SMS listening'),
                action: SnackBarAction(
                  label: 'Settings',
                  onPressed: openAppSettings,
                ),
              ),
            );
          }
          return; // Don't enable if permission denied
        }
      }
    }

    await prefs.setBool('sms_listening_enabled', value);
    setState(() {
      _smsListeningEnabled = value;
    });

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            value
                ? 'SMS listening enabled. App will now automatically process financial SMS.'
                : 'SMS listening disabled. App will no longer process SMS in background.',
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black87,
        elevation: 0,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              children: [
                const SizedBox(height: 16),
                _buildSectionHeader('SMS Integration'),
                _buildSwitchTile(
                  title: 'SMS Transaction Auto-Sync',
                  subtitle: 'Automatically process financial SMS messages for transactions',
                  value: _smsListeningEnabled,
                  onChanged: _toggleSmsListening,
                  icon: Icons.sms,
                ),
                if (!_smsListeningEnabled)
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Manual SMS Processing Available',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: Colors.blue,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'You can still manually process SMS messages using the SMS processing feature in the dashboard.',
                            style: TextStyle(
                              color: Colors.blue.shade700,
                              fontSize: 14,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                const Divider(),
                _buildSectionHeader('Permissions'),
                _buildInfoTile(
                  title: 'SMS Permission',
                  subtitle: 'Required for automatic SMS processing',
                  icon: Icons.perm_phone_msg,
                  onTap: () async {
                    final status = await Permission.sms.status;
                    if (status.isGranted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('SMS permission is granted')),
                      );
                    } else {
                      openAppSettings();
                    }
                  },
                ),
              ],
            ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.black54,
          letterSpacing: 0.5,
        ),
      ),
    );
  }

  Widget _buildSwitchTile({
    required String title,
    required String subtitle,
    required bool value,
    required ValueChanged<bool> onChanged,
    required IconData icon,
  }) {
    return SwitchListTile(
      secondary: Icon(icon, color: Colors.blue),
      title: Text(
        title,
        style: const TextStyle(fontWeight: FontWeight.w500),
      ),
      subtitle: Text(subtitle),
      value: value,
      onChanged: onChanged,
      activeColor: Colors.blue,
    );
  }

  Widget _buildInfoTile({
    required String title,
    required String subtitle,
    required IconData icon,
    required VoidCallback onTap,
  }) {
    return ListTile(
      leading: Icon(icon, color: Colors.blue),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w500)),
      subtitle: Text(subtitle),
      trailing: const Icon(Icons.navigate_next),
      onTap: onTap,
    );
  }
}
