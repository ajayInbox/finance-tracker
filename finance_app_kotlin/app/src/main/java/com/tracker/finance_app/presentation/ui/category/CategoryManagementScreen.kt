package com.tracker.finance_app.presentation.ui.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.domain.model.Category
import com.tracker.finance_app.domain.model.TransactionType

// FinFlow Color Tokens
private val FinFlowText = Color(0xFF18253A)
private val FinFlowMuted = Color(0xFF657188)
private val FinFlowBorder = Color(0xFFDCE1E8)
private val FinFlowBorderLight = Color(0xFFEDF1F5)
private val FinFlowGreen = Color(0xFF087B3D)
private val FinFlowGreenDark = Color(0xFF08783B)
private val FinFlowGreenSoft = Color(0xFFEDF9F1)
private val FinFlowRed = Color(0xFFF04B4B)
private val FinFlowRedDark = Color(0xFFD93434)
private val FinFlowRedSoft = Color(0xFFFFF3F3)
private val FinFlowAppBg = Color(0xFFFBFCFD)
private val FinFlowCardBg = Color(0xFFFFFFFF)

private fun parseColorSafely(hex: String?, fallback: Color = FinFlowGreen): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToAddGroup: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Separate into parent categories (groups) and calculate total subcategories
    val parentGroups = remember(uiState.categories) {
        if (uiState.categories.any { it.parentId == null }) {
            uiState.categories.filter { it.parentId == null }
        } else {
            // Group flat categories by groupName if parentId is absent
            uiState.categories.groupBy { it.groupName }.map { (groupName, list) ->
                val first = list.first()
                Category(
                    id = first.id,
                    name = groupName,
                    type = first.type,
                    iconName = first.iconName,
                    colorHex = first.colorHex,
                    children = list
                )
            }
        }
    }

    val totalSubcategories = remember(parentGroups) {
        parentGroups.sumOf { it.children.size }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinFlowAppBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar (Back button + Title, NO plus icon)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FinFlowText
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Categories",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowText,
                    letterSpacing = (-0.4).sp
                )
            }

            // 2. Section Meta Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALL CATEGORIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowMuted,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "${parentGroups.size} Groups • $totalSubcategories Subcategories",
                    fontSize = 12.sp,
                    color = FinFlowMuted
                )
            }

            // 3. Category Group Cards Accordion List
            if (uiState.isLoading && parentGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FinFlowGreen)
                }
            } else if (parentGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No categories yet. Tap '+ New Category Group' below to add one.",
                        color = FinFlowMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(parentGroups, key = { it.id.ifBlank { it.name } }) { group ->
                        val isExpanded = uiState.expandedGroupIds.contains(group.id)
                        CategoryGroupAccordionCard(
                            group = group,
                            isExpanded = isExpanded,
                            onToggleExpand = { viewModel.toggleGroupExpanded(group.id) },
                            onAddSubcategory = { viewModel.openAddSubcategorySheet(group) },
                            onEditGroup = { viewModel.openEditGroupSheet(group) },
                            onEditSubcategory = { sub -> viewModel.openEditSubcategorySheet(sub, group) },
                            onRequestDeleteGroup = { viewModel.requestDeleteGroup(group) },
                            onDeleteSubcategory = { viewModel.deleteSubcategory(it) }
                        )
                    }
                }
            }
        }

        // 4. Floating Bottom Action Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FinFlowAppBg.copy(alpha = 0f),
                            FinFlowAppBg.copy(alpha = 0.95f),
                            FinFlowAppBg
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Button(
                onClick = { viewModel.openAddGroupSheet() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = FinFlowGreen.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinFlowGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Category Group",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // 5. Bottom Sheet Modal (Create / Edit)
    if (uiState.isBottomSheetOpen) {
        CategoryBottomSheet(
            uiState = uiState,
            parentGroups = parentGroups,
            onClose = { viewModel.closeBottomSheet() },
            onLevelChanged = { viewModel.setSheetLevel(it) },
            onCategoryTypeChanged = { viewModel.onCategoryTypeChanged(it) },
            onParentSelected = { viewModel.onParentGroupSelected(it) },
            onNameChanged = { viewModel.onInputNameChanged(it) },
            onColorSelected = { viewModel.onColorSelected(it) },
            onIconSelected = { viewModel.onIconSelected(it) },
            onSave = { viewModel.saveCategory() }
        )
    }

    // 6. Parent Delete Confirmation Alert Dialog
    uiState.groupToDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteGroup() },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FinFlowRedSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = FinFlowRedDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete \"${group.name}\"?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this group? All ${group.children.size} subcategories under it will also be deleted. Existing transaction records will be preserved.",
                    fontSize = 14.sp,
                    color = FinFlowMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteGroup() },
                    colors = ButtonDefaults.buttonColors(containerColor = FinFlowRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Group", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDeleteGroup() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = FinFlowText)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Accordion Card for Parent Category Group with Inline Subcategories
 */
@Composable
private fun CategoryGroupAccordionCard(
    group: Category,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddSubcategory: () -> Unit,
    onEditGroup: () -> Unit,
    onEditSubcategory: (Category) -> Unit,
    onRequestDeleteGroup: () -> Unit,
    onDeleteSubcategory: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "ChevronRotation"
    )

    val groupColor = remember(group.colorHex) {
        parseColorSafely(group.colorHex, FinFlowGreen)
    }

    val isIncome = group.type == TransactionType.INCOME

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FinFlowCardBg),
        border = BorderStroke(1.dp, FinFlowBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header Row (Clickable to Expand / Collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(groupColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.iconName ?: "📁",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title + Type Pill + Subcategory Count
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = group.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Subtle Category Type Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isIncome) Color(0xFFECFDF5) else Color(0xFFFFF1F2))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isIncome) "Income" else "Expense",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isIncome) Color(0xFF059669) else Color(0xFFE11D48)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${group.children.size} subcategories",
                        fontSize = 12.sp,
                        color = FinFlowMuted
                    )
                }

                // Trailing Actions: 3-Dots Menu + Chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // 3-Dots Overflow Button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Group options",
                                tint = FinFlowMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Group", color = FinFlowText, fontWeight = FontWeight.Medium) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = FinFlowText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEditGroup()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Group", color = FinFlowRedDark, fontWeight = FontWeight.Medium) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = FinFlowRedDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onRequestDeleteGroup()
                                }
                            )
                        }
                    }

                    // Chevron Arrow (Rotates on Expand)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = FinFlowMuted,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(rotation)
                        )
                    }
                }
            }

            // Expanded Subcategories Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(color = FinFlowBorderLight, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(6.dp))

                    if (group.children.isEmpty()) {
                        Text(
                            text = "No subcategories yet.",
                            fontSize = 13.sp,
                            color = FinFlowMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        group.children.forEach { sub ->
                            val subDotColor = remember(sub.colorHex) {
                                parseColorSafely(sub.colorHex, groupColor)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onEditSubcategory(sub) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Subcategory Color Dot
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(subDotColor)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = sub.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FinFlowText,
                                    modifier = Modifier.weight(1f)
                                )

                                // Edit Subcategory Button
                                IconButton(
                                    onClick = { onEditSubcategory(sub) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit subcategory",
                                        tint = FinFlowMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Delete Subcategory Button
                                IconButton(
                                    onClick = { onDeleteSubcategory(sub.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete subcategory",
                                        tint = FinFlowRedDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Inline "+ Add Subcategory" Button
                    OutlinedButton(
                        onClick = onAddSubcategory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FinFlowBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = FinFlowGreen
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Subcategory",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet for Creating / Editing Category Groups and Subcategories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBottomSheet(
    uiState: CategoryUiState,
    parentGroups: List<Category>,
    onClose: () -> Unit,
    onLevelChanged: (CategoryLevel) -> Unit,
    onCategoryTypeChanged: (TransactionType) -> Unit,
    onParentSelected: (Category) -> Unit,
    onNameChanged: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onSave: () -> Unit
) {
    val presetColors = listOf(
        "#087B3D", "#F97316", "#3B82F6", "#EF4444",
        "#8B5CF6", "#EC4899", "#14B8A6", "#64748B"
    )

    val presetIcons = listOf(
        "🍽️", "🛒", "🚗", "🏠", "💡", "🛍️",
        "🎬", "🏥", "💼", "💰", "☕", "✈️"
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val titleText = if (uiState.isEditing) {
        if (uiState.sheetLevel == CategoryLevel.PARENT_GROUP) "Edit Category Group" else "Edit Subcategory"
    } else {
        if (uiState.sheetLevel == CategoryLevel.PARENT_GROUP) "New Category Group" else "Add Subcategory"
    }

    val buttonText = if (uiState.isEditing) "Save Changes" else "Save Category"

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowText
                )
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = FinFlowMuted)
                }
            }

            // 1. Level Toggle: Parent Group vs Subcategory (Only show if creating, or show locked badge if editing)
            if (!uiState.isEditing) {
                Text(
                    text = "Category Level",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinFlowText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(3.dp)
                ) {
                    val isGroup = uiState.sheetLevel == CategoryLevel.PARENT_GROUP
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isGroup) Color.White else Color.Transparent)
                            .clickable { onLevelChanged(CategoryLevel.PARENT_GROUP) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Parent Group",
                            fontSize = 13.sp,
                            fontWeight = if (isGroup) FontWeight.Bold else FontWeight.Medium,
                            color = if (isGroup) FinFlowText else FinFlowMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isGroup) Color.White else Color.Transparent)
                            .clickable { onLevelChanged(CategoryLevel.SUBCATEGORY) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Subcategory",
                            fontSize = 13.sp,
                            fontWeight = if (!isGroup) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isGroup) FinFlowText else FinFlowMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Category Type (For Parent Group)
            if (uiState.sheetLevel == CategoryLevel.PARENT_GROUP) {
                Text(
                    text = "Category Type *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinFlowText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, FinFlowBorder, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExpense = uiState.selectedCategoryType == TransactionType.EXPENSE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExpense) FinFlowRedSoft else Color.Transparent)
                            .clickable { onCategoryTypeChanged(TransactionType.EXPENSE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↘ Expense",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isExpense) FinFlowRedDark else FinFlowMuted
                        )
                    }

                    val isIncome = uiState.selectedCategoryType == TransactionType.INCOME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isIncome) FinFlowGreenSoft else Color.Transparent)
                            .clickable { onCategoryTypeChanged(TransactionType.INCOME) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗ Income",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isIncome) FinFlowGreen else FinFlowMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            } else {
                // 3. Parent Group Selector (For Subcategory)
                Text(
                    text = "Parent Group *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinFlowText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                var expandedDropdown by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, FinFlowBorder, RoundedCornerShape(14.dp))
                        .clickable { expandedDropdown = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.selectedParentGroup?.name ?: "Select parent group",
                            fontSize = 14.sp,
                            color = if (uiState.selectedParentGroup != null) FinFlowText else FinFlowMuted
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = FinFlowMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        parentGroups.forEach { parent ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(parent.iconName ?: "📁")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${parent.name} (${parent.type.name})")
                                    }
                                },
                                onClick = {
                                    onParentSelected(parent)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Name Field
            Text(
                text = "Name *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinFlowText,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = uiState.inputName,
                onValueChange = onNameChanged,
                placeholder = {
                    Text(
                        text = if (uiState.sheetLevel == CategoryLevel.PARENT_GROUP) "e.g. Food & Dining, Bills" else "e.g. Groceries, Coffee, Fuel",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FinFlowGreen,
                    unfocusedBorderColor = FinFlowBorder
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Color Palette
            Text(
                text = "Color Theme",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinFlowText,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presetColors) { hex ->
                    val color = parseColorSafely(hex, FinFlowGreen)
                    val isSelected = uiState.selectedColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(hex) }
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.5.dp, FinFlowText, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Icon Grid
            Text(
                text = "Icon",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinFlowText,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetIcons.take(6).forEach { icon ->
                    val isSelected = uiState.selectedIconKey == icon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FinFlowGreenSoft else Color.White)
                            .border(1.dp, if (isSelected) FinFlowGreen else FinFlowBorder, RoundedCornerShape(12.dp))
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 20.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetIcons.drop(6).take(6).forEach { icon ->
                    val isSelected = uiState.selectedIconKey == icon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FinFlowGreenSoft else Color.White)
                            .border(1.dp, if (isSelected) FinFlowGreen else FinFlowBorder, RoundedCornerShape(12.dp))
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error display
            if (!uiState.error.isNullOrBlank()) {
                Text(
                    text = uiState.error,
                    fontSize = 12.sp,
                    color = FinFlowRedDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 7. Save / Update Button
            Button(
                onClick = onSave,
                enabled = uiState.inputName.isNotBlank() && !uiState.isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinFlowGreen)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(buttonText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
