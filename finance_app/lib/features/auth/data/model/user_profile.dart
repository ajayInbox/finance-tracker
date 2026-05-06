class UserProfile {
  const UserProfile({
    required this.name,
    required this.email,
    required this.isSubscribed,
  });

  final String name;
  final String email;
  final bool isSubscribed;

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      name: json['name'] as String? ?? '',
      // Backend returns "Email" with capital E
      email: (json['Email'] ?? json['email'] ?? '') as String,
      // Backend returns "isSuscribed" (note the typo)
      isSubscribed: (json['isSuscribed'] ?? json['isSubscribed'] ?? false) as bool,
    );
  }

  /// Returns the user's initials (up to 2 characters) for avatar display.
  String get initials {
    final parts = name.trim().split(RegExp(r'\s+'));
    if (parts.isEmpty || parts.first.isEmpty) return '?';
    if (parts.length == 1) return parts.first[0].toUpperCase();
    return '${parts.first[0]}${parts.last[0]}'.toUpperCase();
  }
}
