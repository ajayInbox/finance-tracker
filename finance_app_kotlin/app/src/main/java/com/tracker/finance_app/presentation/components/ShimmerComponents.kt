package com.tracker.finance_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shimmer(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.width(100.dp).height(16.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(150.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun ShimmerDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ShimmerProfile()
        ShimmerCard(modifier = Modifier.fillMaxWidth().height(150.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerCard(modifier = Modifier.weight(1f).height(100.dp))
            ShimmerCard(modifier = Modifier.weight(1f).height(100.dp))
        }
        ShimmerCard(modifier = Modifier.fillMaxWidth().height(80.dp))
        Box(modifier = Modifier.width(120.dp).height(20.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).shimmer())
        ShimmerList()
    }
}

@Composable
fun ShimmerProfile(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().shimmer(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).background(Color.Gray.copy(alpha = 0.3f), CircleShape))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Box(modifier = Modifier.width(80.dp).height(12.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(120.dp).height(20.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().shimmer()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(Color.Gray.copy(alpha = 0.3f), CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.width(100.dp).height(16.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(60.dp).height(12.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
            }
            Box(modifier = Modifier.width(50.dp).height(20.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun ShimmerList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            ShimmerListItem()
        }
    }
}
