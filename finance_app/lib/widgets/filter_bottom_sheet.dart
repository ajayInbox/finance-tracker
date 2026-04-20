import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class FilterBottomSheet extends StatefulWidget {
  final String selectedTimeFilter;
  final String selectedAccountFilter;
  final String selectedCategoryFilter;
  final Function(String, String, String) onFiltersChanged;
  final VoidCallback onClearAll;
  final ScrollController? scrollController;

  const FilterBottomSheet({
    super.key,
    required this.selectedTimeFilter,
    required this.selectedAccountFilter,
    required this.selectedCategoryFilter,
    required this.onFiltersChanged,
    required this.onClearAll,
    this.scrollController,
  });

  @override
  State<FilterBottomSheet> createState() => _FilterBottomSheetState();
}

class _FilterBottomSheetState extends State<FilterBottomSheet> {
  late String _timeFilter;
  late String _accountFilter;
  late String _categoryFilter;

  @override
  void initState() {
    super.initState();
    _timeFilter = widget.selectedTimeFilter;
    _accountFilter = widget.selectedAccountFilter;
    _categoryFilter = widget.selectedCategoryFilter;
  }

  void _applyFilters() {
    widget.onFiltersChanged(_timeFilter, _accountFilter, _categoryFilter);
    Navigator.of(context).pop();
  }

  void _clearAllFilters() {
    setState(() {
      _timeFilter = 'All';
      _accountFilter = 'All';
      _categoryFilter = 'All';
    });
    widget.onClearAll();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(40)),
      ),
      padding: const EdgeInsets.fromLTRB(24, 16, 24, 24),
      child: SafeArea(
        child: SingleChildScrollView(
          controller: widget.scrollController,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
            // Handle bar
            Center(
              child: Container(
                width: 48,
                height: 6,
                decoration: BoxDecoration(
                  color: Colors.grey[200],
                  borderRadius: BorderRadius.circular(3),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Filters',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: const Color(0xFF111827),
                  ),
                ),
                TextButton(
                  onPressed: _clearAllFilters,
                  style: TextButton.styleFrom(
                    foregroundColor: const Color(0xFF10B981),
                  ),
                  child: Text(
                    'Clear all',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Time Period Filter
            _buildSectionHeader('Time Period'),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                'All',
                'Today',
                'Yesterday',
                'This Week',
                'This Month',
                'Custom Range'
              ].map((filter) {
                return _buildFilterChip(
                  filter,
                  _timeFilter == filter,
                  () => setState(() => _timeFilter = filter),
                );
              }).toList(),
            ),
            const SizedBox(height: 32),

            // Account Filter
            _buildSectionHeader('Account'),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                'All',
                'Main Account',
                'Savings',
                'Credit Card'
              ].map((filter) {
                return _buildFilterChip(
                  filter,
                  _accountFilter == filter,
                  () => setState(() => _accountFilter = filter),
                );
              }).toList(),
            ),
            const SizedBox(height: 32),

            // Category Filter
            _buildSectionHeader('Category'),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                'All',
                'Food',
                'Transport',
                'Shopping',
                'Entertainment'
              ].map((filter) {
                return _buildFilterChip(
                  filter,
                  _categoryFilter == filter,
                  () => setState(() => _categoryFilter = filter),
                );
              }).toList(),
            ),
            const SizedBox(height: 48),

            // Apply Button
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _applyFilters,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF111827),
                  foregroundColor: Colors.white,
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                child: Text(
                  'Apply Filters',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    )
    );
  }

  Widget _buildSectionHeader(String title) {
    return Text(
      title,
      style: GoogleFonts.plusJakartaSans(
        fontSize: 16,
        fontWeight: FontWeight.w700,
        color: const Color(0xFF111827),
      ),
    );
  }

  Widget _buildFilterChip(
    String label,
    bool isSelected,
    VoidCallback onTap,
  ) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF10B981) : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: isSelected ? null : Border.all(color: Colors.grey[200]!),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: const Color(0xFF10B981).withValues(alpha: 0.4),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ]
              : null,
        ),
        child: Text(
          label,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: isSelected ? Colors.white : Colors.grey[500],
          ),
        ),
      ),
    );
  }
}
