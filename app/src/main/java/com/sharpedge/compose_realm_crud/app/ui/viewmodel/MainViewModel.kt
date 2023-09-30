package com.sharpedge.compose_realm_crud.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpedge.compose_realm_crud.app.data.model.Expense
import com.sharpedge.compose_realm_crud.app.data.repository.ExpenseRepository
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
        val error: String? = null
    )

    private val _state = MutableStateFlow(ViewState())
    //val state: StateFlow<ViewState> = _state

    val debugState = _state.onEach { Log.d("Sarmad", "State has been updated: $it") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)
    val state: StateFlow<ViewState> = debugState



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
                _state.value = _state.value.copy(error = "Failed to load data.", isLoading = false)
            }
        }


    }


    fun addExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(expense)
            loadExpenses()  // Refresh the list after adding
            clearSelectedExpense() // Make sure the input fields are cleared
        }
    }


    fun updateExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedExpense = state.value.selectedExpense!!
            if (expense.expenseName.isEmpty()) {
                expense.expenseName = selectedExpense.expenseName
            }
            if (expense.date.isEmpty()) {
                expense.date = selectedExpense.date
            }

            repository.updateExpense(expense) // pass to repo
            loadExpenses()  // Refresh the list after updating
            clearSelectedExpense() // Make sure the input fields are cleared
        }
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