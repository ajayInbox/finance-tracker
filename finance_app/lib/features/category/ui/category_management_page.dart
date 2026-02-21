import 'package:finance_app/features/category/ui/add_category_group_page.dart';
import 'package:finance_app/features/category/ui/add_category_page.dart';
import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/features/category/application/category_controller.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

class CategoryManagementPage extends ConsumerStatefulWidget {
  const CategoryManagementPage({super.key});

  @override
  ConsumerState<CategoryManagementPage> createState() =>
      _CategoryManagementPageState();
}

class _CategoryManagementPageState
    extends ConsumerState<CategoryManagementPage> {
  final TextEditingController _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final categoriesAsync = ref.watch(categoryControllerProvider);
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
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            backgroundColor: backgroundColor,
            surfaceTintColor: Colors.transparent,
            pinned: true,
            centerTitle: true,
            leading: IconButton(
              icon: Icon(Icons.chevron_left, color: Colors.grey[500], size: 28),
              onPressed: () => Navigator.of(context).pop(),
            ),
            title: Text(
              'Manage Categories',
              style: GoogleFonts.plusJakartaSans(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: textColor,
              ),
            ),
            actions: [
              IconButton(
                icon: Icon(Icons.search, color: Colors.grey[500]),
                onPressed: () {
                  // Focus search or show search bar
                },
              ),
            ],
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: _buildSearchBar(surfaceColor, primaryColor),
            ),
          ),
          categoriesAsync.when(
            data: (categories) {
              return SliverPadding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 8,
                ),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    ...categories.map((group) {
                      int codePoint = 0;
                      String fontFamily = '';
                      List<String> iconParts = group.iconKey!.split('+');
                      if (iconParts.length == 2) {
                        codePoint = int.parse(iconParts[0]);
                        fontFamily = iconParts[1];
                      }
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 16.0),
                        child: _buildCategoryGroup(
                          title: group.name,
                          categories: group.children,
                          icon: IconData(codePoint, fontFamily: fontFamily),
                          iconColor: Colors.white,
                          iconBgColor: Color(int.parse(group.colorCode)),
                          surfaceColor: surfaceColor,
                          primaryColor: primaryColor,
                          textColor: textColor,
                        ),
                      );
                    }),
                    const SizedBox(height: 100), // padding for FAB
                  ]),
                ),
              );
            },
            loading: () => const SliverFillRemaining(
              child: Center(
                child: CircularProgressIndicator(color: Color(0xFF13EC5B)),
              ),
            ),
            error: (error, stack) => SliverFillRemaining(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Center(
                  child: Text(
                    'Error loading categories:\n$error',
                    style: GoogleFonts.plusJakartaSans(color: Colors.red),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (context) => const AddCategoryGroupPage(),
            ),
          );
        },
        backgroundColor: primaryColor,
        elevation: 4,
        child: const Icon(Icons.add, color: Color(0xFF102216)),
      ),
    );
  }

  Widget _buildSearchBar(Color surfaceColor, Color primaryColor) {
    return Container(
      decoration: BoxDecoration(
        color: surfaceColor,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.02),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: TextField(
        controller: _searchController,
        // Removed search logic temporarily as it relies on flattening the tree
        decoration: InputDecoration(
          hintText: 'Filter categories...',
          hintStyle: GoogleFonts.plusJakartaSans(
            color: Colors.grey[400],
            fontSize: 14,
          ),
          prefixIcon: Icon(Icons.filter_list, color: Colors.grey[400]),
          border: InputBorder.none,
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: BorderSide(color: primaryColor, width: 2),
          ),
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 14,
          ),
        ),
      ),
    );
  }

  Widget _buildCategoryGroup({
    required String title,
    required List<Category> categories,
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required Color surfaceColor,
    required Color primaryColor,
    required Color textColor,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: surfaceColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.withValues(alpha: 0.1)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.02),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: Theme(
          data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
          child: ExpansionTile(
            initiallyExpanded: true,
            tilePadding: const EdgeInsets.symmetric(
              horizontal: 20,
              vertical: 8,
            ),
            title: Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: iconBgColor,
                    shape: BoxShape.circle,
                  ),
                  child: Icon(icon, color: iconColor),
                ),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: GoogleFonts.plusJakartaSans(
                        fontWeight: FontWeight.w700,
                        fontSize: 16,
                        color: textColor,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${categories.length} sub-categories',
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 12,
                        color: Colors.grey[500],
                      ),
                    ),
                  ],
                ),
              ],
            ),
            children: [
              Container(
                decoration: BoxDecoration(
                  border: Border(
                    top: BorderSide(color: Colors.grey.withValues(alpha: 0.1)),
                  ),
                  color: Colors.black.withValues(alpha: 0.02),
                ),
                child: Column(
                  children: [
                    ...categories.map(
                      (cat) => _buildCategoryItem(cat, textColor, primaryColor),
                    ),
                    InkWell(
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (context) => const AddCategoryPage(),
                          ),
                        );
                      },
                      child: Container(
                        width: double.infinity,
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.add, size: 18, color: primaryColor),
                            const SizedBox(width: 8),
                            Text(
                              'ADD TO ${title.toUpperCase()}',
                              style: GoogleFonts.plusJakartaSans(
                                fontSize: 12,
                                fontWeight: FontWeight.w700,
                                color: primaryColor,
                                letterSpacing: 0.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCategoryItem(
    Category category,
    Color textColor,
    Color primaryColor,
  ) {
    // Determine color marker based on active status or hash
    final int hash = category.name.hashCode;
    final List<Color> colors = [
      Colors.red,
      Colors.green,
      Colors.blue,
      Colors.orange,
      Colors.purple,
      Colors.cyan,
      Colors.pink,
    ];
    final Color markerColor = colors[hash % colors.length];

    return InkWell(
      onTap: () {},
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
        decoration: BoxDecoration(
          border: Border(
            bottom: BorderSide(color: Colors.grey.withValues(alpha: 0.1)),
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 4,
              height: 32,
              decoration: BoxDecoration(
                color: markerColor,
                borderRadius: BorderRadius.circular(4),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Text(
                category.name,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: textColor,
                ),
              ),
            ),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.edit, size: 20),
                  color: Colors.grey[400],
                  onPressed: () {},
                  splashRadius: 20,
                  constraints: const BoxConstraints(),
                  padding: const EdgeInsets.all(8),
                ),
                IconButton(
                  icon: const Icon(Icons.delete, size: 20),
                  color: Colors.grey[400],
                  onPressed: () {},
                  splashRadius: 20,
                  constraints: const BoxConstraints(),
                  padding: const EdgeInsets.all(8),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
