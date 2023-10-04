package com.sharpedge.compose_realm_crud.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpedge.compose_realm_crud.app.data.model.Expense
import com.sharpedge.compose_realm_crud.app.data.repository.ExpenseRepository
import com.sharpedge.compose_realm_crud.app.utils.isNullOrBlankOrEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    val repository: ExpenseRepository
) : ViewModel() {


    data class ViewState(
        val expenses: List<Expense> = emptyList(),
        val selectedExpense: Expense? = null,
        val isLoading: Boolean = false,
        val error: ErrorType = ErrorType.None
    )

    private val _state = MutableStateFlow(ViewState())
    //val state: StateFlow<ViewState> = _state

    val debugState = _state.onEach { Log.d("Sarmad", "State has been updated: $it") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)
    val state: StateFlow<ViewState> = debugState


    private val _clearEvent = MutableStateFlow(false)
    val clearEvent: StateFlow<Boolean> = _clearEvent


    private fun triggerInputError(errorMessage: String) {
        Log.e("triggerError", "is called")
        // Assuming other properties remain the same, just changing the error here
        _state.value = _state.value.copy(error = ErrorType.TextFieldError(errorMessage))

    }

    fun clearError() {
        _state.value = _state.value.copy(error = ErrorType.None)
    }


    fun initialize() {
        initializeData()
        loadExpenses()
    }

    private fun initializeData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshData()
        }
    }

    private fun loadExpenses() {

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            try {
                repository.expensesFlow.collect { expenses ->
                    _state.value = _state.value.copy(expenses = expenses, isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ErrorType.LazyListError("Failed to load data."),
                    isLoading = false
                )
            }
        }


    }

    private fun clearInputFields() {
        _clearEvent.value = true
    }

    fun resetClearInputFieldsFlag() {
        _clearEvent.value = false
    }

    fun addExpense(expenseName: String, expenseAmount: String, date: String) {
        Log.e("addExpense", "is called")
        if (isExpenseDataValid(
                expenseName,
                expenseAmount,
                date
            )
        ) { // check the validity of input first
            val expense = Expense().apply {
                this.expenseName = expenseName
                this.amount = expenseAmount.toDouble()
                this.date = date

            }
            // now we can clear input fields
            clearInputFields()
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertExpense(expense)
                loadExpenses()  // Refresh the list after adding
                clearSelectedExpense() // Make sure the input fields are cleared
            }
        }

    }

    private fun isExpenseDataValid(
        expenseName: String,
        expenseAmount: String,
        date: String
    ): Boolean {
        Log.e("isExpenseDataValid", "is called")
        var isValid = true
        if (expenseName.isNullOrBlankOrEmpty()) {
            isValid = false
            triggerInputError("Please enter expense name")
        } else if (expenseName.length <= 1) {
            isValid = false
            triggerInputError("Please enter a valid expense name")
        } else if (expenseAmount.isNullOrBlankOrEmpty()) {
            isValid = false
            triggerInputError("Please enter amount")
        } else if (expenseAmount.toDouble() <= 0) { // don't want 0 expense or negative value
            isValid = false
            triggerInputError("Please enter a valid amount")
        } else if (date.isNullOrBlankOrEmpty()) {
            isValid = false
            triggerInputError("Please enter date")
        }

        return isValid
    }


    fun updateExpense(
        expenseName: String ,
        expenseAmount: String,
        date: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val selectedExpense = state.value.selectedExpense!!
            val expenseForUpdate = getExpenseForUpdate(selectedExpense, expenseName, expenseAmount, date)


            if (isDataValidForUpdate(expenseName, expenseAmount, date)) {
                repository.updateExpense(expenseForUpdate) // pass to repo
                loadExpenses()  // Refresh the list after updating
                clearSelectedExpense() // Make sure the input fields are cleared
            } else {
                triggerInputError("All the fields cannot be empty")
            }

        }
    }

    // Creating a separate function for validate, since we can use the previous values of data if anything is empty


    private fun getExpenseForUpdate(
        currentExpense: Expense?,
        expenseName: String = "",
        expenseAmount: String = "",
        date: String = ""
    ): Expense {
        return Expense().apply {
            this.expenseName = if (expenseName.isNullOrBlankOrEmpty()) currentExpense?.expenseName
                ?: "" else expenseName
            this.amount = if (expenseAmount.isNullOrBlankOrEmpty()) currentExpense?.amount
                ?: 0.0 else expenseAmount.toDouble()
            this.date = if (date.isNullOrBlankOrEmpty()) currentExpense?.date ?: "" else date
            this.id = currentExpense!!.id
        }
    }

    private fun isDataValidForUpdate(
        expenseName: String = "",
        expenseAmount: String = "",
        date: String = ""
    ): Boolean {
        return !expenseName.isNullOrBlankOrEmpty() && !expenseAmount.isNullOrBlankOrEmpty() && !date.isNullOrBlankOrEmpty()
    }


    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
            loadExpenses()
        }

    }

    fun selectExpense(expense: Expense) {
        _state.value = _state.value.copy(selectedExpense = expense)
    }

    fun clearSelectedExpense() {
        _state.value = _state.value.copy(selectedExpense = null)
    }
}