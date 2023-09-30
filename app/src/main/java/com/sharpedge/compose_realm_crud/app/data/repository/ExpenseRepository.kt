package com.sharpedge.compose_realm_crud.app.data.repository

import com.sharpedge.compose_realm_crud.app.data.realm.DataSource
import com.sharpedge.compose_realm_crud.app.data.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseRepository(private val dataSource: DataSource) {

    private val _expensesFlow: MutableStateFlow<List<Expense>> = MutableStateFlow(emptyList())
    val expensesFlow: Flow<List<Expense>> = _expensesFlow.asStateFlow()

    // Insert a new expense into the Realm database
    suspend fun insertExpense(expense: Expense) {

        dataSource.insert(expense)
        refreshData()

    }

    // Update an existing expense in the Realm database
    suspend fun updateExpense(expense: Expense) {

        dataSource.update(expense)
        refreshData()
    }

    // Delete an expense from the Realm database
    suspend fun deleteExpense(expense: Expense) {

        dataSource.delete(expense)
        refreshData()

    }

    suspend fun refreshData() {

        val expenses = dataSource.fetchAll()
        _expensesFlow.value = expenses
    }

}