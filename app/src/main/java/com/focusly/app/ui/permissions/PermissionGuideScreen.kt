package com.focusly.app.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.focusly.app.domain.manager.PermissionHelper
import com.focusly.app.ui.theme.GreenPrimary

@Composable
fun PermissionGuideScreen(
    onAllGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionHelper = remember { PermissionHelper(context) }

    var hasUsageStats by remember { mutableStateOf(permissionHelper.hasUsageStatsPermission()) }
    var hasOverlay by remember { mutableStateOf(permissionHelper.hasOverlayPermission()) }
    var hasAccessibility by remember { mutableStateOf(permissionHelper.isAccessibilityServiceEnabled()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = permissionHelper.hasUsageStatsPermission()
                hasOverlay = permissionHelper.hasOverlayPermission()
                hasAccessibility = permissionHelper.isAccessibilityServiceEnabled()

                if (hasUsageStats && hasOverlay && hasAccessibility) {
                    onAllGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to Focusly",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To track screen time accurately and shield you from distractions, Focusly requires 3 standard Android permissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PermissionItemCard(
            title = "1. Usage Access",
            description = "Allows Focusly to measure total screen time and time spent inside specific apps.",
            icon = Icons.Default.DataUsage,
            isGranted = hasUsageStats,
            onGrantClick = { permissionHelper.requestUsageStatsPermission() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionItemCard(
            title = "2. Display Over Other Apps",
            description = "Enables Focusly to draw the mindfulness shield screen when a restricted app is opened.",
            icon = Icons.Default.Layers,
            isGranted = hasOverlay,
            onGrantClick = { permissionHelper.requestOverlayPermission() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionItemCard(
            title = "3. Accessibility Service",
            description = "Used strictly to detect when a blocked app is brought to the foreground.",
            icon = Icons.Default.Accessibility,
            isGranted = hasAccessibility,
            onGrantClick = { permissionHelper.requestAccessibilityPermission() }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAllGranted,
            enabled = hasUsageStats && hasOverlay && hasAccessibility,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue to Focusly", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isGranted) GreenPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else icon,
                        contentDescription = null,
                        tint = if (isGranted) GreenPrimary else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        text = if (isGranted) "Granted" else "Action required",
                        fontSize = 12.sp,
                        color = if (isGranted) GreenPrimary else Color(0xFFFF9500),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission", fontSize = 14.sp)
                }
            }
        }
    }
}
