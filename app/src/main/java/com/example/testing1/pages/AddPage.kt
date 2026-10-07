// ── AddExpenseScreen.kt ───────────────────────────────────────────────────────
package com.example.testing1.pages

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.testing1.viewmodel.AddExpenseUiState
import com.example.testing1.viewmodel.AddExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgDark        = Color(0xFF121212)
private val Surface       = Color(0xFF1E1E1E)
private val Primary       = Color(0xFF6C63FF)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFAAAAAA)
private val TextMuted     = Color(0xFF666666)
private val CardShape     = RoundedCornerShape(12.dp)
private val PillShape     = RoundedCornerShape(50.dp)

private data class Category(val id: String, val emoji: String, val label: String)
private val Categories = listOf(
    Category("food",      "🍔", "Food"),
    Category("transport", "🚗", "Transport"),
    Category("shopping",  "🛍️", "Shopping"),
    Category("bills",     "📄", "Bills"),
    Category("health",    "💊", "Health"),
    Category("fun",       "🎮", "Fun"),
    Category("education", "📚", "Education"),
    Category("other",     "📦", "Other"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPage( modifier: Modifier = Modifier,
    onSaved: () -> Unit = {},
    vm: AddExpenseViewModel = viewModel()
) {
    var isLoading by remember {
        mutableStateOf(false)
    }
    val uiState by vm.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is AddExpenseUiState.Success) {
            vm.resetState()
        }
    }

    var amount           by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("food") }
    var note             by remember { mutableStateOf("") }
    var photoUri         by remember { mutableStateOf<Uri?>(null) }
    var amountError      by remember { mutableStateOf(false) }

    // ── Date picker state ──────────────────────────────────────────────────
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    // ── Photo picker ───────────────────────────────────────────────────────
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 100.dp)
    ) {
        Text("Add Expense", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, modifier = Modifier.padding(bottom = 20.dp))

        // ── Amount ────────────────────────────────────────────────────────
        SectionLabel("AMOUNT")
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = if (amountError) 4.dp else 20.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; amountError = false },
                placeholder = { Text("0.00", color = TextMuted, fontSize = 18.sp) },
                isError = amountError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                colors = fieldColors(),
                shape = CardShape,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(start = 28.dp)
            )
            Text("$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
        }
        if (amountError) {
            Text("Please enter a valid amount", fontSize = 11.sp, color = Color(0xFFCF6679),
                modifier = Modifier.padding(bottom = 16.dp))
        }

        // ── Category grid ─────────────────────────────────────────────────
        SectionLabel("CATEGORY")
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false
        ) {
            items(Categories) { cat ->
                CategoryChip(
                    category = cat,
                    isSelected = selectedCategory == cat.id,
                    onClick = { selectedCategory = cat.id }
                )
            }
        }

        // ── Note ──────────────────────────────────────────────────────────
        SectionLabel("NOTE (OPTIONAL)")
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            placeholder = { Text("e.g. Lunch with team", color = TextMuted, fontSize = 13.sp) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 13.sp),
            colors = fieldColors(),
            shape = CardShape,
            modifier = Modifier.fillMaxWidth().height(75.dp).padding(bottom = 20.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // ── Date picker ───────────────────────────────────────────────────
        SectionLabel("DATE")
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Text("📅", fontSize = 18.sp,
                    modifier = Modifier.clickable { showDatePicker = true }.padding(end = 12.dp))
            },
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 12.sp),
            colors = fieldColors(),
            shape = CardShape,
            modifier = Modifier.fillMaxWidth().height(75.dp)
                .clickable { showDatePicker = true }.padding(bottom = 20.dp)
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK", color = Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = Surface)
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor          = Surface,
                        titleContentColor       = TextPrimary,
                        headlineContentColor    = TextPrimary,
                        weekdayContentColor     = TextSecondary,
                        subheadContentColor     = TextSecondary,
                        yearContentColor        = TextPrimary,
                        currentYearContentColor = Primary,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = Primary,
                        dayContentColor          = TextPrimary,
                        selectedDayContentColor  = Color.White,
                        selectedDayContainerColor = Primary,
                        todayContentColor        = Primary,
                        todayDateBorderColor     = Primary,
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // ── Photo ─────────────────────────────────────────────────────────
        SectionLabel("PHOTO (OPTIONAL)")
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Expense photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .clip(CardShape).padding(bottom = 8.dp)
            )
        }
        OutlinedButton(
            onClick = { photoPicker.launch("image/*") },
            shape = CardShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Text(
                if (photoUri == null) "📷  Attach Photo" else "📷  Change Photo",
                color = Primary, fontSize = 13.sp
            )
        }

        // ── Submit ────────────────────────────────────────────────────────
        Button(
            onClick = {
                isLoading = true

                if (amount.toDoubleOrNull() == null) {
                    amountError = true
                    isLoading = false
                    return@Button
                }

                vm.saveExpense(amount, selectedCategory, note, selectedDateMillis, photoUri)
                Handler(Looper.getMainLooper()).postDelayed({
                    isLoading = false
                }, 1500)            },
            enabled = !isLoading,
            shape = CardShape,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                text = if (isLoading) "Adding Expense..." else "Add Expense",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CategoryChip(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(if (isSelected) Primary.copy(alpha = 0.15f) else Surface, CardShape)
            .border(1.dp, if (isSelected) Primary else Color.Transparent, CardShape)
            .clickable(onClick = onClick).padding(10.dp)
    ) {
        Text(category.emoji, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(category.label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Primary,
    unfocusedBorderColor = Surface,
    cursorColor          = Primary,
    focusedContainerColor   = Surface,
    unfocusedContainerColor = Surface,
)