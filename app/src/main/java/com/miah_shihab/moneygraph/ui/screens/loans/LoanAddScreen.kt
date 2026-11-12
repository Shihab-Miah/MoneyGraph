package com.miah_shihab.moneygraph.ui.screens.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miah_shihab.moneygraph.data.repository.LoanRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.domain.model.LoanType
import com.miah_shihab.moneygraph.ui.theme.ExpenseRed
import com.miah_shihab.moneygraph.ui.theme.IncomeGreen
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanAddScreen(
    loanRepository: LoanRepository,
    preferencesRepository: PreferencesRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(LoanType.LENT) }

    val currencySymbol by preferencesRepository.currencySymbolFlow.collectAsState(initial = "৳")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Loan / Debt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector Tab
            val tabs = listOf(LoanType.LENT to "I Lent (Gave Money)", LoanType.BORROWED to "I Borrowed (Took Money)")
            val selectedIndex = if (selectedType == LoanType.LENT) 0 else 1

            TabRow(selectedTabIndex = selectedIndex) {
                tabs.forEachIndexed { index, (type, label) ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { selectedType = type },
                        text = { Text(label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Person Name Input
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person / Counterparty Name") },
                placeholder = { Text("e.g. John Doe, Family, Friend") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            currencySymbol,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == LoanType.LENT) IncomeGreen else ExpenseRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            placeholder = { Text("0.00", fontSize = 28.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Purpose (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val amount = amountText.toBigDecimalSafe()
                    if (personName.isNotBlank() && amount > BigDecimal.ZERO) {
                        scope.launch {
                            loanRepository.insertLoan(
                                personName = personName.trim(),
                                amount = amount,
                                type = selectedType,
                                note = noteText.trim()
                            )
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = personName.isNotBlank() && amountText.toBigDecimalSafe() > BigDecimal.ZERO
            ) {
                Text("Save Loan Record", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
