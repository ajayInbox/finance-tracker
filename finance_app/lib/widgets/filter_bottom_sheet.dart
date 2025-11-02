import 'package:flutter/material.dart';

class FilterBottomSheet extends StatefulWidget {
  final String selectedTimeFilter;
  final String selectedAccountFilter;
  final String selectedCategoryFilter;
  final Function(String, String, String) onFiltersChanged;
  final VoidCallback onClearAll;

  const FilterBottomSheet({
    super.key,
    required this.selectedTimeFilter,
    required this.selectedAccountFilter,
    required this.selectedCategoryFilter,
    required this.onFiltersChanged,
    required this.onClearAll
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
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Filters',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              TextButton(
                onPressed: _clearAllFilters,
                child: const Text('Clear all'),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Time Period Filter
          const Text(
            'Time Period',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              'All',
              'Today',
              'Yesterday',
              'This Week',
              'This Month',
              'Custom Range'
            ].map((filter) {
              final isSelected = _timeFilter == filter;
              return FilterChip(
                label: Text(filter),
                selected: isSelected,
                onSelected: (selected) {
                  setState(() {
                    _timeFilter = filter;
                  });
                },
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Account Filter
          const Text(
            'Account',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              'All',
              'Main Account',
              'Savings',
              'Credit Card'
            ].map((filter) {
              final isSelected = _accountFilter == filter;
              return FilterChip(
                label: Text(filter),
                selected: isSelected,
                onSelected: (selected) {
                  setState(() {
                    _accountFilter = filter;
                  });
                },
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Category Filter
          const Text(
            'Category',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              'All',
              'Food',
              'Transport',
              'Shopping',
              'Entertainment'
            ].map((filter) {
              final isSelected = _categoryFilter == filter;
              return FilterChip(
                label: Text(filter),
                selected: isSelected,
                onSelected: (selected) {
                  setState(() {
                    _categoryFilter = filter;
                  });
                },
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Apply Button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _applyFilters,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: const Text('Apply Filters'),
            ),
          ),
        ],
      ),
    );
  }
}
