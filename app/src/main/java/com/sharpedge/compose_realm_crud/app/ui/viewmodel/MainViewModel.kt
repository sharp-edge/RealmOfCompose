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
    val state: StateFlow<ViewState> = _state

//    val debugState = _state.onEach { Log.d("Sarmad", "State has been updated: $it") }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)
//    val state: StateFlow<ViewState> = debugState


    private val _clearEvent = MutableStateFlow(false)
    val clearEvent: StateFlow<Boolean> = _clearEvent


    // This function is called only when an input type error needs to be informed to the View.
    public fun triggerInputError(errorMessage: String) {
        //Log.e("triggerError", "is called")
        // Assuming other properties remain the same, just changing the error here
        _state.value = _state.value.copy(error = ErrorType.TextFieldError(errorMessage))

    }

    // this function is called from the View to clear the error state after the user has been informed of the error
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

    // This function gets all the data AKA Expenses from the Repository. Repository further calls the DataSource for the data, Data Source gets the data from local Realm
    public fun loadExpenses() {

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

    // This function is used to clear input fields from the View, in case the input data is passed through validation and sent to Repository to add/update
    private fun clearInputFields() {
        _clearEvent.value = true
    }

    // This function is called from the View in order to change the state of composable to allow input data after clearing it.
    fun resetClearInputFieldsFlag() {
        _clearEvent.value = false
    }

    // This function gets the input values, and after success validation, passes them to Repository function
    fun addExpense(expenseName: String, expenseAmount: String, date: String) {
        //Log.e("addExpense", "is called")
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

    // Just as the name states, this function checks the validity of the input data passed for the Add operation in CRUD
    public fun isExpenseDataValid(
        expenseName: String,
        expenseAmount: String,
        date: String
    ): Boolean {
        //Log.e("isExpenseDataValid", "is called")
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


    // This function gets the input values, and after success validation, passes them to Repository function for update operation of CRUD
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

    // This function creates an Expense Object based on the passed input values, if any value passed is empty, then existing selected Expense object's value is used instead.
    // Basically this is for update only, when the user click on a list item to update, the list Expense object is stored in ViewState as Selected Expense.
    public fun getExpenseForUpdate(
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

    // Just as the name states, this function checks if all the values are Null/Empty/Blank for the update operation in CRUD
    public fun isDataValidForUpdate(
        expenseName: String = "",
        expenseAmount: String = "",
        date: String = ""
    ): Boolean {
        return !expenseName.isNullOrBlankOrEmpty() && !expenseAmount.isNullOrBlankOrEmpty() && !date.isNullOrBlankOrEmpty()
    }


    // Deletes an expense, when the user long clicks a list item, a confirmation dialog appears in View then if user selects yes, the selected Expense object is passed to Repository to perform delete operation in CRUD
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
            loadExpenses()
        }

    }

    // When ever user clicks a list item in the View, this function stores the Expense object
    fun selectExpense(expense: Expense) {
        _state.value = _state.value.copy(selectedExpense = expense)
    }

    // This function is called in multiple places, there is a clear button in View which clears the text fields and the Selected Expense object
    fun clearSelectedExpense() {
        _state.value = _state.value.copy(selectedExpense = null)
    }
}