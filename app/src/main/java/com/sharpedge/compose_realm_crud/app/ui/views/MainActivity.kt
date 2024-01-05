package com.sharpedge.compose_realm_crud.app.ui.views

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sharpedge.compose_realm_crud.app.data.model.Expense
import com.sharpedge.compose_realm_crud.app.ui.viewmodel.MainViewModel
import com.sharpedge.compose_realm_crud.ui.theme.AndroidCRUDJetpackComposeRealmTheme
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharpedge.compose_realm_crud.app.ui.viewmodel.ErrorType
import java.time.LocalDate
import java.util.Calendar

class MainActivity : ComponentActivity() {


    private val viewModel: MainViewModel by viewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidCRUDJetpackComposeRealmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.inversePrimary
                            ),
                            title = {
                                Text(text = "Jetpack MVVM Flow Realm")
                            },

                            )
                        MainContent()

                    }
                }
            }
        }
        viewModel.initialize()

    }


    @Composable
    fun MainContent() {
        Box(modifier =  Modifier.fillMaxSize()) {
            val viewState by viewModel.state.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewState.error) {
                viewState.error?.let { errorType ->
                    if (errorType is ErrorType.TextFieldError) {
                        val errorMessage = errorType.message

                        val result = snackbarHostState.showSnackbar(
                            message = errorMessage,
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Short
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed, SnackbarResult.Dismissed -> viewModel.clearError()
                        }
                    }
                }
            }


            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {

                ExpenseInputFields(
                    expense = viewState.selectedExpense,
                    onUpdate = { expenseName, expenseAmount, date ->
                        viewModel.updateExpense(expenseName, expenseAmount, date)

                    },
                    onAdd = { expenseName, expenseAmount, date ->
                        viewModel.addExpense(expenseName, expenseAmount, date)
                    },
                    onClear = {
                        viewModel.clearSelectedExpense()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                ExpensesList(
                    expenses = viewState.expenses,
                    isLoading = viewState.isLoading,
                    error = viewState.error,
                    onExpenseClick = { clickedExpense ->
                        viewModel.selectExpense(clickedExpense)
                    }
                )
            }


        }

    }


    @Composable
    fun ExpenseInputFields(
        expense: Expense?,
        onUpdate: (String, String, String) -> Unit,
        onAdd: (String, String, String) -> Unit,
        onClear: () -> Unit
    ) {

        var expenseName by remember(expense) { mutableStateOf(expense?.expenseName ?: "") }
        var amount by remember(expense) { mutableStateOf(expense?.amount?.toString() ?: "") }
        var date by remember(expense) { mutableStateOf(expense?.date ?: "") }



        val clearEvent by viewModel.clearEvent.collectAsStateWithLifecycle()
        if (clearEvent) {
            expenseName = ""
            amount = ""
            date = ""
            viewModel.resetClearInputFieldsFlag()
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {

            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = expenseName,
                onValueChange = { expenseName = it },
                label = { Text("Expense Name") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        // Create input fields using the variables above.
        // ...

        // If a list item is selected (i.e., expense is not null), show "Update" and "Clear" buttons


        val dateNow = remember { mutableStateOf(LocalDate.now()) }
        val showDatePicker = remember { mutableStateOf(false) }
        val context = LocalContext.current

        DisposableEffect(showDatePicker.value) {
            if (showDatePicker.value) {
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        dateNow.value = LocalDate.of(year, month + 1, dayOfMonth)
                        showDatePicker.value = false
                    },
                    dateNow.value.year,
                    dateNow.value.monthValue - 1,
                    dateNow.value.dayOfMonth
                )
                datePickerDialog.show()

                onDispose {
                    datePickerDialog.dismiss()
                }
            }
            onDispose {}
        }


        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val current = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                date = "$year-${month + 1}-$dayOfMonth"
                            },
                            current.get(Calendar.YEAR),
                            current.get(Calendar.MONTH),
                            current.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )},
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = date,
                onValueChange = { },
                readOnly = true,
                enabled = false,
                label = { Text("Date") },
                singleLine = true,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (expense != null) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 15.dp, end = 20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp,
                        disabledElevation = 0.dp
                    ),
                    onClick = {
                        onClear()  // Clear the state in ViewModel
                    }) {
                    Text("Clear")
                }


                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 15.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp,
                        disabledElevation = 0.dp
                    ),
                    onClick = {
                        onUpdate(expenseName, amount, date)
                    }) {
                    Text("Update")
                }

            } else {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 15.dp, start = 15.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp,
                        disabledElevation = 0.dp
                    ),
                    onClick = {
                        onAdd(expenseName, amount, date)  // Call the provided lambda with the new expense data

                    }) {
                    Text("Add")
                }
            }

        }

    }


    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        AndroidCRUDJetpackComposeRealmTheme {
            Greeting("Android")
        }

    }


    @Composable
    fun ExpensesList(
        expenses: List<Expense>,
        isLoading: Boolean,
        error: ErrorType?,
        onExpenseClick: (Expense) -> Unit
    ) {
        var showDeleteDialog by remember { mutableStateOf(false) }
        var selectedExpenseForDeletion by remember { mutableStateOf<Expense?>(null) }
        Log.e("ExpensesList()", "expense is ${expenses.size}")

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                // Loading state
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (error != null && error is ErrorType.LazyListError) {
                // Error state
                Text(text = error.message, Modifier.align(Alignment.Center))
            } else {
                if (expenses.isEmpty()) {
                    Text(text = "No Expenses", Modifier.align(Alignment.Center))
                } else {

                }
                LazyColumn {
                    items(expenses.size) { index ->
                        val expense = expenses[index]
                        ExpenseItem(
                            expense = expense,
                            onExpenseClick = onExpenseClick,
                            onExpenseLongClick = {
                                selectedExpenseForDeletion = expense
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }


        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    selectedExpenseForDeletion?.let {
                        viewModel.deleteExpense(it)
                    }
                    showDeleteDialog = false
                }
            )
        }
    }



    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ExpenseItem(
        expense: Expense,
        onExpenseClick: (Expense) -> Unit,
        onExpenseLongClick: (Expense) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onExpenseClick(expense) },
                        onLongClick = { onExpenseLongClick(expense) }
                    )
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.expenseName,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        //color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "£${expense.amount}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        //color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Expense",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }



    @Composable
    fun DeleteConfirmationDialog(
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = "Delete Item") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismissRequest()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("No")
                }
            }
        )
    }

}

