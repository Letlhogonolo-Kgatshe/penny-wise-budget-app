// ── HistoryScreen.kt ──────────────────────────────────────────────────────────
package com.example.testing1.pages

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.testing1.model.ExpenseModel
import com.example.testing1.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgDark        = Color(0xFF121212)
private val Surface       = Color(0xFF1E1E1E)
private val Primary       = Color(0xFF6C63FF)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFAAAAAA)
private val TextMuted     = Color(0xFF666666)
private val ErrorRed      = Color(0xFFCF6679)
private val HistCardShape = RoundedCornerShape(16.dp)

private val categoryEmoji = mapOf(
    "food" to "🍔", "transport" to "🚗", "shopping" to "🛍️",
    "bills" to "📄", "health" to "💊", "fun" to "🎮",
    "education" to "📚", "other" to "📦"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage( modifier: Modifier = Modifier,vm: HistoryViewModel = viewModel()) {

    val expenses   by vm.expenses.collectAsState()
    val fromMillis by vm.fromMillis.collectAsState()
    val toMillis   by vm.toMillis.collectAsState()

    var expandedPhoto  by remember { mutableStateOf<String?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }

    val fromState = rememberDatePickerState(initialSelectedDateMillis = fromMillis)
    val toState   = rememberDatePickerState(initialSelectedDateMillis = toMillis)

    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize().background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text("Expense History", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp))

        // ── Date range selector ───────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRangeButton(label = "From: ${fmt.format(Date(fromMillis))}",
                modifier = Modifier.weight(1f)) { showFromPicker = true }
            DateRangeButton(label = "To: ${fmt.format(Date(toMillis))}",
                modifier = Modifier.weight(1f)) { showToPicker = true }
        }

        if (showFromPicker) {
            DateRangePickerDialog(
                state = fromState,
                onDismiss = { showFromPicker = false },
                onConfirm = {
                    fromState.selectedDateMillis?.let { vm.setRange(it, toMillis) }
                    showFromPicker = false
                }
            )
        }
        if (showToPicker) {
            DateRangePickerDialog(
                state = toState,
                onDismiss = { showToPicker = false },
                onConfirm = {
                    toState.selectedDateMillis?.let { vm.setRange(fromMillis, it) }
                    showToPicker = false
                }
            )
        }

        // ── List ──────────────────────────────────────────────────────────
        if (expenses.isEmpty()) {
            Text("No expenses in this period", fontSize = 13.sp, color = TextMuted)
        } else {
            expenses.forEach { expense ->
                HistoryRow(
                    expense   = expense,
                    onDelete  = { vm.deleteExpense(expense) },
                    onPhotoClick = { expandedPhoto = expense.photoUri }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── Full-screen photo viewer ──────────────────────────────────────────
    expandedPhoto?.let { uri ->
        Dialog(onDismissRequest = { expandedPhoto = null }) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E)).clickable { expandedPhoto = null }) {
                AsyncImage(
                    model = Uri.parse(uri),
                    contentDescription = "Expense photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
                Text("Tap to close", fontSize = 11.sp, color = TextSecondary,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
            }
        }
    }
}

@Composable
private fun HistoryRow(
    expense: ExpenseModel,
    onDelete: () -> Unit,
    onPhotoClick: () -> Unit
) {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(Surface, HistCardShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(categoryEmoji[expense.category] ?: "📦", fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.note ?: expense.category.replaceFirstChar { it.uppercase() },
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(fmt.format(Date(expense.dateMillis)), fontSize = 11.sp, color = TextSecondary)
        }
        // Photo thumbnail if available
        if (expense.photoUri != null) {
            AsyncImage(
                model = Uri.parse(expense.photoUri),
                contentDescription = "Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onPhotoClick).padding(end = 8.dp)
            )
        }
        Text("-$${"%,.2f".format(expense.amount)}", fontSize = 13.sp,
            fontWeight = FontWeight.Bold, color = Primary,
            modifier = Modifier.padding(end = 4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete",
                tint = ErrorRed, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK", color = Primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } },
        colors = DatePickerDefaults.colors(containerColor = Surface)
    ) {
        DatePicker(state = state,
            colors = DatePickerDefaults.colors(
                containerColor = Surface, titleContentColor = TextPrimary,
                headlineContentColor = TextPrimary, dayContentColor = TextPrimary,
                selectedDayContentColor = Color.White, selectedDayContainerColor = Primary,
                todayContentColor = Primary, todayDateBorderColor = Primary))
    }
}

@Composable
fun DateRangeButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary)) {
        Text(label, fontSize = 11.sp, color = Primary)
    }
}