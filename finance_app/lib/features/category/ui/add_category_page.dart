import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/features/category/application/category_controller.dart';

class AddCategoryPage extends ConsumerStatefulWidget {
  final Category? categoryToEdit;

  const AddCategoryPage({super.key, this.categoryToEdit});

  @override
  ConsumerState<AddCategoryPage> createState() => _AddCategoryPageState();
}

class _AddCategoryPageState extends ConsumerState<AddCategoryPage> {
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _iconSearchController = TextEditingController();

  Category? _selectedParentGroup;
  int _selectedColorIndex = 0;
  IconData _selectedIcon = Icons.shopping_cart;

  final List<Color> _themeColors = [
    const Color(0xFF13EC5B), // primary theme color
    Colors.teal,
    Colors.amber,
    Colors.pink,
    Colors.purple,
    Colors.cyan,
  ];

  final List<IconData> _availableIcons = [
    Icons.shopping_cart,
    Icons.restaurant,
    Icons.directions_car,
    Icons.home,
    Icons.flight,
    Icons.medical_services,
    Icons.school,
    Icons.fitness_center,
    Icons.pets,
    Icons.movie,
    Icons.local_gas_station,
    Icons.checkroom,
    Icons.videogame_asset,
    Icons.wifi,
    Icons.more_horiz,
  ];

  @override
  void initState() {
    super.initState();
    if (widget.categoryToEdit != null) {
      final category = widget.categoryToEdit!;
      _nameController.text = category.name;

      List<String> iconParts = category.iconKey.split('+');
      if (iconParts.length == 2) {
        int codePoint = int.parse(iconParts[0]);
        String fontFamily = iconParts[1];
        _selectedIcon = IconData(codePoint, fontFamily: fontFamily);
        for (var icon in _availableIcons) {
          if (icon.codePoint == codePoint && icon.fontFamily == fontFamily) {
            _selectedIcon = icon;
            break;
          }
        }
      }

      int colorCode = int.tryParse(category.colorCode) ?? 0;
      if (colorCode != 0) {
        Color c = Color(colorCode);
        int idx = _themeColors.indexWhere(
          (element) => element.value == c.value,
        );
        if (idx != -1) {
          _selectedColorIndex = idx;
        }
      }

      // the parent group will be set in the build method when the groups are loaded
      // we'll use a post frame callback or just rely on the future builder
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _iconSearchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final backgroundColor = isDark
        ? const Color(0xFF102216)
        : const Color(0xFFF6F8F6);
    final surfaceColor = isDark ? const Color(0xFF1C2D21) : Colors.white;
    final textColor = isDark ? Colors.white : const Color(0xFF111827);
    final primaryColor = const Color(0xFF13EC5B);

    return Scaffold(
      backgroundColor: backgroundColor,
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(textColor, primaryColor),
            Expanded(
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    _buildPreviewSection(primaryColor, textColor),
                    const SizedBox(height: 8),
                    _buildParentGroupSection(primaryColor, textColor),
                    Padding(
                      padding: const EdgeInsets.symmetric(vertical: 24),
                      child: Divider(
                        height: 1,
                        color: Colors.grey.withValues(alpha: 0.2),
                      ),
                    ),
                    _buildAppearanceSection(
                      primaryColor,
                      textColor,
                      surfaceColor,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(Color textColor, Color primaryColor) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(color: Colors.grey.withValues(alpha: 0.1)),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: Text(
              'Cancel',
              style: GoogleFonts.plusJakartaSans(
                color: Colors.grey[500],
                fontSize: 16,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          Text(
            widget.categoryToEdit == null ? 'New Category' : 'Edit Category',
            style: GoogleFonts.plusJakartaSans(
              color: textColor,
              fontSize: 18,
              fontWeight: FontWeight.w700,
            ),
          ),
          TextButton(
            onPressed: () async {
              final name = _nameController.text.trim();
              if (name.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Please enter a category name')),
                );
                return;
              }
              if (_selectedParentGroup == null) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Please select a parent group')),
                );
                return;
              }

              try {
                showDialog(
                  context: context,
                  barrierDismissible: false,
                  builder: (context) =>
                      const Center(child: CircularProgressIndicator()),
                );

                final data = {
                  "name": name,
                  "type": _selectedParentGroup!.type,
                  "parentId": _selectedParentGroup!.id,
                  "iconKey":
                      '${_selectedIcon.codePoint}+${_selectedIcon.fontFamily ?? ''}',
                  "colorCode": _themeColors[_selectedColorIndex]
                      .toARGB32()
                      .toString(),
                };

                if (widget.categoryToEdit == null) {
                  await ref
                      .read(categoryControllerProvider.notifier)
                      .createCategory(data);
                } else {
                  await ref
                      .read(categoryControllerProvider.notifier)
                      .updateCategory(widget.categoryToEdit!.id, data);
                }

                if (mounted) {
                  Navigator.of(context).pop(); // dismiss loading
                  Navigator.of(context).pop(); // close page
                }
              } catch (e) {
                if (mounted) {
                  Navigator.of(context).pop(); // dismiss
                  ScaffoldMessenger.of(
                    context,
                  ).showSnackBar(SnackBar(content: Text('Error: $e')));
                }
              }
            },
            child: Text(
              widget.categoryToEdit == null ? 'Save' : 'Update',
              style: GoogleFonts.plusJakartaSans(
                color: primaryColor,
                fontSize: 16,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPreviewSection(Color primaryColor, Color textColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 32),
      child: Column(
        children: [
          Stack(
            alignment: Alignment.bottomRight,
            children: [
              Container(
                width: 96,
                height: 96,
                decoration: BoxDecoration(
                  color: _themeColors[_selectedColorIndex],
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: _themeColors[_selectedColorIndex].withValues(
                        alpha: 0.2,
                      ),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Icon(_selectedIcon, color: Colors.white, size: 48),
              ),
              Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: Colors.grey[800],
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.black, width: 2),
                ),
                child: const Icon(Icons.edit, color: Colors.white, size: 14),
              ),
            ],
          ),
          const SizedBox(height: 24),
          TextField(
            controller: _nameController,
            textAlign: TextAlign.center,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 24,
              fontWeight: FontWeight.w700,
              color: textColor,
            ),
            decoration: InputDecoration(
              hintText: 'Category Name',
              hintStyle: GoogleFonts.plusJakartaSans(color: Colors.grey[400]),
              border: UnderlineInputBorder(
                borderSide: BorderSide(
                  color: Colors.grey.withValues(alpha: 0.3),
                  width: 2,
                ),
              ),
              enabledBorder: UnderlineInputBorder(
                borderSide: BorderSide(
                  color: Colors.grey.withValues(alpha: 0.3),
                  width: 2,
                ),
              ),
              focusedBorder: UnderlineInputBorder(
                borderSide: BorderSide(color: primaryColor, width: 2),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildParentGroupSection(Color primaryColor, Color textColor) {
    final categoriesState = ref.watch(categoryControllerProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Parent Group',
                style: GoogleFonts.plusJakartaSans(
                  color: textColor,
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                ),
              ),
              Text(
                'See all',
                style: GoogleFonts.plusJakartaSans(
                  color: primaryColor,
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 40,
          child: categoriesState.when(
            data: (categories) {
              if (categories.isEmpty) {
                return Center(
                  child: Text(
                    'No parent groups found.',
                    style: GoogleFonts.plusJakartaSans(
                      color: Colors.grey[500],
                      fontSize: 14,
                    ),
                  ),
                );
              }

              // Only top-level groups
              final groups = categories;

              // Pre-select parent group if editing
              if (widget.categoryToEdit != null &&
                  _selectedParentGroup == null) {
                try {
                  final parent = groups.firstWhere(
                    (g) => g.id == widget.categoryToEdit!.parentId,
                  );
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    if (mounted) {
                      setState(() {
                        _selectedParentGroup = parent;
                      });
                    }
                  });
                } catch (_) {}
              }

              return ListView.separated(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                scrollDirection: Axis.horizontal,
                itemCount: groups.length + 1,
                separatorBuilder: (context, index) => const SizedBox(width: 12),
                itemBuilder: (context, index) {
                  if (index == groups.length) {
                    return _buildNewGroupButton(primaryColor);
                  }

                  final group = groups[index];
                  final isSelected = _selectedParentGroup?.id == group.id;

                  return InkWell(
                    onTap: () {
                      setState(() {
                        _selectedParentGroup = group;
                      });
                    },
                    borderRadius: BorderRadius.circular(20),
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: isSelected
                            ? primaryColor
                            : Colors.grey.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(20),
                        boxShadow: isSelected
                            ? [
                                BoxShadow(
                                  color: primaryColor.withValues(alpha: 0.3),
                                  blurRadius: 6,
                                  offset: const Offset(0, 2),
                                ),
                              ]
                            : null,
                      ),
                      child: Text(
                        group.name,
                        style: GoogleFonts.plusJakartaSans(
                          color: isSelected ? Colors.white : Colors.grey[500],
                          fontSize: 14,
                          fontWeight: isSelected
                              ? FontWeight.w600
                              : FontWeight.w500,
                        ),
                      ),
                    ),
                  );
                },
              );
            },
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, s) => Center(child: Text('Error loading groups')),
          ),
        ),
      ],
    );
  }

  Widget _buildNewGroupButton(Color primaryColor) {
    return InkWell(
      onTap: () {
        // Implement new group action
      },
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          border: Border.all(
            color: Colors.grey.withValues(alpha: 0.4),
            style: BorderStyle
                .none, // To simulate dashed we just use plain for now
          ),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          '+ New',
          style: GoogleFonts.plusJakartaSans(
            color: Colors.grey[500],
            fontSize: 14,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
    );
  }

  Widget _buildAppearanceSection(
    Color primaryColor,
    Color textColor,
    Color surfaceColor,
  ) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Color Theme',
            style: GoogleFonts.plusJakartaSans(
              color: textColor,
              fontSize: 16,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              ...List.generate(
                _themeColors.length,
                (index) => InkWell(
                  onTap: () {
                    setState(() {
                      _selectedColorIndex = index;
                    });
                  },
                  child: Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: _themeColors[index],
                      shape: BoxShape.circle,
                      border: _selectedColorIndex == index
                          ? Border.all(color: Colors.white, width: 2)
                          : null,
                      boxShadow: _selectedColorIndex == index
                          ? [
                              BoxShadow(
                                color: _themeColors[index].withValues(
                                  alpha: 0.5,
                                ),
                                blurRadius: 4,
                                spreadRadius: 2,
                              ),
                            ]
                          : null,
                    ),
                  ),
                ),
              ),
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: Colors.grey.withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.add, color: Colors.grey[500]),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Text(
            'Select Icon',
            style: GoogleFonts.plusJakartaSans(
              color: textColor,
              fontSize: 16,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(
              color: surfaceColor,
              borderRadius: BorderRadius.circular(12),
            ),
            child: TextField(
              controller: _iconSearchController,
              decoration: InputDecoration(
                icon: Icon(Icons.search, color: Colors.grey[400], size: 20),
                hintText: 'Search icons...',
                hintStyle: GoogleFonts.plusJakartaSans(
                  color: Colors.grey[400],
                  fontSize: 14,
                ),
                border: InputBorder.none,
              ),
            ),
          ),
          const SizedBox(height: 16),
          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 5,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
            ),
            itemCount: _availableIcons.length,
            itemBuilder: (context, index) {
              final icon = _availableIcons[index];
              final isSelected = _selectedIcon == icon;
              final selectedThemeColor = _themeColors[_selectedColorIndex];

              return InkWell(
                onTap: () {
                  setState(() {
                    _selectedIcon = icon;
                  });
                },
                borderRadius: BorderRadius.circular(12),
                child: Container(
                  decoration: BoxDecoration(
                    color: isSelected
                        ? selectedThemeColor.withValues(alpha: 0.2)
                        : surfaceColor,
                    border: isSelected
                        ? Border.all(color: selectedThemeColor, width: 2)
                        : null,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    icon,
                    color: isSelected ? selectedThemeColor : Colors.grey[500],
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }
}
