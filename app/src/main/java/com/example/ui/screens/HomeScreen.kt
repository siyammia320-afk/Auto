package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.BrandTertiary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    userPassword: String,
    masterProxy: String,
    activeNumbersCount: Int,
    otpCount: Int,
    onNavigate: (AppNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "NumGo Live",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto Facebook Creator & Number Panel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(
                            label = "পাসওয়ার্ড:",
                            value = if (userPassword.isNotEmpty()) userPassword else "সেট নেই ❌",
                            isOk = userPassword.isNotEmpty()
                        )
                        StatusChip(
                            label = "প্রক্সি:",
                            value = if (masterProxy.isNotEmpty()) "সক্রিয় ✅" else "নিষ্ক্রিয় ❌",
                            isOk = masterProxy.isNotEmpty()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(
                            label = "সক্রিয় নাম্বার:",
                            value = "$activeNumbersCount টি",
                            isOk = activeNumbersCount > 0
                        )
                        StatusChip(
                            label = "প্রাপ্ত OTP:",
                            value = "$otpCount টি",
                            isOk = otpCount > 0
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "প্রধান মেনু",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuActionCard(
                    title = "🎲 GET NUMBER",
                    subtitle = "ভার্চুয়াল নাম্বার নেওয়া",
                    icon = Icons.Default.PhoneAndroid,
                    color = BrandPrimary,
                    tag = "btn_get_number",
                    onClick = { onNavigate(AppNavScreen.GET_NUMBER) },
                    modifier = Modifier.weight(1f)
                )

                MenuActionCard(
                    title = "🚀 Auto Create",
                    subtitle = "অটো ফেসবুক আইডি তৈরি",
                    icon = Icons.Default.RocketLaunch,
                    color = SuccessGreen,
                    tag = "btn_auto_create",
                    onClick = { onNavigate(AppNavScreen.AUTO_CREATE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuActionCard(
                    title = "🔑 Set Password",
                    subtitle = "আইডির পাসওয়ার্ড সেট",
                    icon = Icons.Default.Key,
                    color = BrandSecondary,
                    tag = "btn_set_password",
                    onClick = { onNavigate(AppNavScreen.SET_PASSWORD) },
                    modifier = Modifier.weight(1f)
                )

                MenuActionCard(
                    title = "🔐 2FA CODE",
                    subtitle = "TOTP কোড জেনারেটর",
                    icon = Icons.Default.Lock,
                    color = WarningAmber,
                    tag = "btn_2fa_code",
                    onClick = { onNavigate(AppNavScreen.TWO_FACTOR) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuActionCard(
                    title = "🎯 OTP Monitor",
                    subtitle = "লাইভ ওটিপি ইনবক্স",
                    icon = Icons.Default.MarkEmailRead,
                    color = BrandTertiary,
                    tag = "btn_otp_monitor",
                    onClick = { onNavigate(AppNavScreen.OTP_MONITOR) },
                    modifier = Modifier.weight(1f)
                )

                MenuActionCard(
                    title = "🌐 Proxy Settings",
                    subtitle = "প্রক্সি কনফিগারেশন",
                    icon = Icons.Default.Settings,
                    color = MaterialTheme.colorScheme.primary,
                    tag = "btn_proxy_settings",
                    onClick = { onNavigate(AppNavScreen.PROXY_SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: String,
    isOk: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isOk) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MenuActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
