package com.miah_shihab.moneygraph.ui.screens.records

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miah_shihab.moneygraph.data.repository.AccountRepository
import com.miah_shihab.moneygraph.data.repository.CategoryRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.data.repository.RecordRepository
import com.miah_shihab.moneygraph.domain.model.Account
import com.miah_shihab.moneygraph.domain.model.Category
import com.miah_shihab.moneygraph.domain.model.CategoryType
import com.miah_shihab.moneygraph.domain.model.RecordType
import com.miah_shihab.moneygraph.ui.theme.ExpenseRed
import com.miah_shihab.moneygraph.ui.theme.IncomeGreen
import com.miah_shihab.moneygraph.ui.theme.TransferBlue
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import com.miah_shihab.moneygraph.util.toLocalDateString
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordAddEditScreen(
    recordId: Long = -1L,
    recordRepository: RecordRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    preferencesRepository: PreferencesRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedType by remember { mutableStateOf(RecordType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableLongStateOf(-1L) }
    var selectedCategoryId by remember { mutableLongStateOf(-1L) }
    var fromAccountId by remember { mutableLongStateOf(-1L) }
    var toAccountId by remember { mutableLongStateOf(-1L) }
    var recordTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val accounts by accountRepository.getAll().collectAsState(initial = emptyList())
    val categories by categoryRepository.getAll().collectAsState(initial = emptyList())
    val currencySymbol by preferencesRepository.currencySymbolFlow.collectAsState(initial = "৳")

    LaunchedEffect(recordId) {
        if (recordId > 0) {
            recordRepository.getById(recordId)?.let { r ->
                selectedType = r.type
                amountText = r.amount.toPlainString()
                noteText = r.note
                selectedAccountId = r.account?.id ?: -1L
                selectedCategoryId = r.category?.id ?: -1L
                fromAccountId = r.fromAccount?.id ?: -1L
                toAccountId = r.toAccount?.id ?: -1L
                recordTime = r.time
            }
        }
    }

    // Set default accounts if not set
    LaunchedEffect(accounts) {
        if (selectedAccountId == -1L && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
            fromAccountId = accounts.first().id
            if (accounts.size > 1) toAccountId = accounts[1].id
        }
    }

    // Filter categories by type
    val filteredCategories = categories.filter {
        when (selectedType) {
            RecordType.INCOME -> it.type == CategoryType.INCOME
            RecordType.EXPENSE -> it.type == CategoryType.EXPENSE
            RecordType.TRANSFER -> false
        }
    }

    LaunchedEffect(filteredCategories) {
        if (selectedCategoryId == -1L && filteredCategories.isNotEmpty()) {
            selectedCategoryId = filteredCategories.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId > 0) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (recordId > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                recordRepository.delete(recordId)
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
                        }
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
            // Type Selector TabRow
            val tabs = listOf(RecordType.EXPENSE to "Expense", RecordType.INCOME to "Income", RecordType.TRANSFER to "Transfer")
            val selectedTabIndex = when (selectedType) {
                RecordType.EXPENSE -> 0
                RecordType.INCOME -> 1
                RecordType.TRANSFER -> 2
            }

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, (type, label) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedType = type
                            selectedCategoryId = -1L
                        },
                        text = { Text(label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Amount Input
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            currencySymbol,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Bold,
                            color = when (selectedType) {
                                RecordType.EXPENSE -> ExpenseRed
                                RecordType.INCOME -> IncomeGreen
                                RecordType.TRANSFER -> TransferBlue
                            }
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

            // Account Selector (For Expense / Income)
            if (selectedType != RecordType.TRANSFER) {
                Column {
                    Text("Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accounts) { acc ->
                            FilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = { selectedAccountId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                }

                // Category Selector
                Column {
                    Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredCategories) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }
            } else {
                // Transfer: From Account & To Account
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("From Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accounts) { acc ->
                            FilterChip(
                                selected = fromAccountId == acc.id,
                                onClick = { fromAccountId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }

                    Text("To Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accounts.filter { it.id != fromAccountId }) { acc ->
                            FilterChip(
                                selected = toAccountId == acc.id,
                                onClick = { toAccountId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            // Date preview
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date", style = MaterialTheme.typography.bodyMedium)
                    Text(recordTime.toLocalDateString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    val amount = amountText.toBigDecimalSafe()
                    if (amount > BigDecimal.ZERO) {
                        scope.launch {
                            if (recordId > 0) {
                                val acc = accounts.find { it.id == selectedAccountId }
                                val cat = categories.find { it.id == selectedCategoryId }
                                val fromAcc = accounts.find { it.id == fromAccountId }
                                val toAcc = accounts.find { it.id == toAccountId }
                                recordRepository.update(
                                    com.miah_shihab.moneygraph.domain.model.Record(
                                        id = recordId,
                                        time = recordTime,
                                        type = selectedType,
                                        amount = amount,
                                        note = noteText.trim(),
                                        account = acc,
                                        category = cat,
                                        fromAccount = fromAcc,
                                        toAccount = toAcc
                                    )
                                )
                            } else {
                                recordRepository.insert(
                                    type = selectedType,
                                    amount = amount,
                                    note = noteText.trim(),
                                    accountId = selectedAccountId,
                                    categoryId = selectedCategoryId,
                                    fromAccountId = fromAccountId,
                                    toAccountId = toAccountId,
                                    time = recordTime
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = amountText.toBigDecimalSafe() > BigDecimal.ZERO
            ) {
                Text(if (recordId > 0) "Update Transaction" else "Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
