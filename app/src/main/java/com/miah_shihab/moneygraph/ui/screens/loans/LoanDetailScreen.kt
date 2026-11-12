package com.miah_shihab.moneygraph.ui.screens.loans

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miah_shihab.moneygraph.data.repository.LoanRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.domain.model.Loan
import com.miah_shihab.moneygraph.domain.model.LoanStatus
import com.miah_shihab.moneygraph.domain.model.LoanType
import com.miah_shihab.moneygraph.ui.theme.ExpenseRed
import com.miah_shihab.moneygraph.ui.theme.IncomeGreen
import com.miah_shihab.moneygraph.ui.theme.IncomeGreenContainer
import com.miah_shihab.moneygraph.ui.theme.LoanOrange
import com.miah_shihab.moneygraph.ui.theme.LoanOrangeContainer
import com.miah_shihab.moneygraph.ui.theme.PrimaryBlue
import com.miah_shihab.moneygraph.ui.theme.TransferBlueContainer
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import com.miah_shihab.moneygraph.util.toCurrencyString
import com.miah_shihab.moneygraph.util.toLocalDateString
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: Long,
    loanRepository: LoanRepository,
    preferencesRepository: PreferencesRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loan by remember { mutableStateOf<Loan?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentNoteText by remember { mutableStateOf("") }

    val currencySymbol by preferencesRepository.currencySymbolFlow.collectAsState(initial = "৳")

    fun reloadLoan() {
        scope.launch {
            loan = loanRepository.getById(loanId)
        }
    }

    LaunchedEffect(loanId) {
        reloadLoan()
    }

    val currentLoan = loan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentLoan?.personName ?: "Loan Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            loanRepository.delete(loanId)
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (currentLoan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
        } else {
            val isLent = currentLoan.type == LoanType.LENT
            val progress = if (currentLoan.amount > BigDecimal.ZERO) {
                currentLoan.paidAmount.divide(currentLoan.amount, 2, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
            } else 0f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Status & Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isLent) "Money Lent to" else "Money Borrowed from",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val (badgeText, badgeBg, badgeColor) = when (currentLoan.status) {
                                LoanStatus.ACTIVE -> Triple("Active", LoanOrangeContainer, LoanOrange)
                                LoanStatus.PARTIAL -> Triple("Partial", TransferBlueContainer, PrimaryBlue)
                                LoanStatus.SETTLED -> Triple("Settled", IncomeGreenContainer, IncomeGreen)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(badgeText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeColor)
                            }
                        }

                        Text(
                            currentLoan.personName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isLent) IncomeGreen else PrimaryBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentLoan.amount.toCurrencyString(currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Repaid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentLoan.paidAmount.toCurrencyString(currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentLoan.remainingAmount.toCurrencyString(currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isLent) IncomeGreen else ExpenseRed)
                            }
                        }

                        if (currentLoan.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Note: ${currentLoan.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Action: Record Repayment Button
                if (currentLoan.status != LoanStatus.SETTLED) {
                    Button(
                        onClick = {
                            paymentAmountText = currentLoan.remainingAmount.toPlainString()
                            showPaymentDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Repayment", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Repayment History Section
                Text(
                    "Repayment History (${currentLoan.payments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (currentLoan.payments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No repayments recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentLoan.payments) { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.time.toLocalDateString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        if (p.note.isNotBlank()) {
                                            Text(p.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        "+${p.amount.toCurrencyString(currencySymbol)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Dialog
            if (showPaymentDialog) {
                AlertDialog(
                    onDismissRequest = { showPaymentDialog = false },
                    title = { Text("Record Repayment") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = paymentAmountText,
                                onValueChange = { paymentAmountText = it },
                                label = { Text("Amount Repaid") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = paymentNoteText,
                                onValueChange = { paymentNoteText = it },
                                label = { Text("Note (Optional)") },
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = paymentAmountText.toBigDecimalSafe()
                                if (amount > BigDecimal.ZERO) {
                                    scope.launch {
                                        loanRepository.addPayment(loanId, amount, paymentNoteText.trim())
                                        showPaymentDialog = false
                                        reloadLoan()
                                    }
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPaymentDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
