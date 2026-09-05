package com.tracker.finance_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HeaderTextDark = Color(0xFF18253A)
private val HeaderBorder = Color(0xFFDCE1E8)
private val UnreadRed = Color(0xFFEF4444)

@Composable
fun ScreenHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.offset(x = (-8).dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HeaderTextDark
                    )
                }
            }

            Text(
                text = title,
                fontSize = if (onBackClick != null) 24.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = HeaderTextDark,
                modifier = if (onBackClick != null) Modifier.offset(x = (-8).dp) else Modifier
            )
        }

        // Notification Button with Unread Indicator
        Box(
            modifier = Modifier
                .size(42.dp)
                .border(1.dp, HeaderBorder, RoundedCornerShape(14.dp))
                .background(Color.White, RoundedCornerShape(14.dp))
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = HeaderTextDark,
                modifier = Modifier.size(22.dp)
            )
            if (hasUnreadNotifications) {
                // Red Unread Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .background(UnreadRed, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
    }
}
