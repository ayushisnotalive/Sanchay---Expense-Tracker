package com.camuel.sanchay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.theme.CashflowIncome
import com.camuel.sanchay.ui.theme.CashflowWarning

@Composable
fun TermsAndConditionsScreen(
    onAccept: () -> Unit
) {
    var isAgreed by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Privacy Policy & User Agreement",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scrollable Agreement Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome to Sanchay. Please review and accept our operational terms and privacy commitments before accessing your personal financial ledger.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                // Clause 1: Offline & Bookkeeping
                AgreementSection(
                    icon = Icons.Default.Lock,
                    title = "1. Personal Bookkeeping & Non-Custodial Nature",
                    content = "Sanchay is purely a self-hosted personal bookkeeping ledger. Sanchay is NOT a bank, payment aggregator, financial institution, or custodial service. The application does not hold, transfer, process, or have custody of any monetary assets or government-backed currency. All account balances and transactions are user-recorded representations for personal tracking."
                )

                // Clause 2: Data Sovereignty
                AgreementSection(
                    icon = Icons.Default.PrivacyTip,
                    title = "2. Absolute Privacy & Local Data Storage",
                    content = "Your privacy is paramount. Sanchay operates offline-first. Transaction records, custom categories, wallet balances, notes, and receipt images are stored exclusively in private on-device storage. Sanchay does NOT read SMS messages (READ_SMS), does not scrape banking apps, and never sells or transmits your financial records to third-party ad networks."
                )

                // Clause 3: Advisory Disclaimer
                AgreementSection(
                    icon = Icons.Default.Security,
                    title = "3. AI Advisory & Calculations Disclaimer",
                    content = "Budget calculations, economic burn rates, 50/30/20 breakdowns, and AI assistant insights are provided solely for informational and personal budgeting assistance. They do not constitute certified financial, tax, or legal advice under SEBI, RBI, or any regulatory authority. Users are solely responsible for their financial decisions."
                )

                // Clause 4: Backups & Ownership
                AgreementSection(
                    icon = Icons.Default.Shield,
                    title = "4. User Control & Data Portability",
                    content = "You maintain complete ownership of your data. You may export your records to JSON, Excel (XLSX), or PDF, or permanently wipe all application data at any time through Executive Settings."
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Last Updated: October 2026 • Target SDK 35 (Android 15 Compliant)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Agreement Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "I have read and agree to the Terms of Service and Privacy Policy.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = onAccept,
                enabled = isAgreed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Accept & Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isAgreed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun AgreementSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = content,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )
    }
}
