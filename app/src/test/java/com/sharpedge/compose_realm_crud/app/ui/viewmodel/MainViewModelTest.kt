package com.sharpedge.compose_realm_crud.app.ui.viewmodel

import com.sharpedge.compose_realm_crud.app.data.model.Expense
import com.sharpedge.compose_realm_crud.app.data.repository.ExpenseRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MainViewModelTest {

//    private lateinit var repository: ExpenseRepository
//    private lateinit var viewModel: MainViewModel
//
//    @Before
//    fun setup() {
//        repository = mockk(relaxed = true)
//        viewModel = MainViewModel(repository)
//    }

    private val repository: ExpenseRepository = mockk(relaxed = true)
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dummyExpense(id: String = UUID.randomUUID().toString()) = Expense(
        id = id,
        date = "2023-10-01",
        expenseName = "Test Expense",
        amount = 123.45
    )

    @Test
    fun `initialize should refresh data and load expenses`() = runTest {
        coEvery { repository.refreshData() } just Runs

        viewModel.initialize()

        coVerify { repository.refreshData() }
        coVerify { repository.expensesFlow }
    }

    @Test
    fun `addExpense with valid data should insert expense and load expenses`() = runTest {
        viewModel.addExpense("test", "10.0", "2023-10-07")

        coVerify {
            repository.insertExpense(any())
            repository.expensesFlow
        }
    }

    @Test
    fun `addExpense with invalid data should not insert expense`() = runTest {
        viewModel.addExpense("", "", "")

        coVerify(exactly = 0) { repository.insertExpense(any()) }
    }

    @Test
    fun `updateExpense with valid data should update expense and load expenses`() = runTest {
        val expense = Expense("1", "2023-10-07", "test", 10.0)
        viewModel.selectExpense(expense)

        viewModel.updateExpense("updatedTest", "20.0", "2023-10-08")

        coVerify {
            repository.updateExpense(any())
            repository.expensesFlow
        }
    }

    @Test
    fun `updateExpense with invalid data should not update expense`() = runTest {
        viewModel.updateExpense("", "", "")

        coVerify(exactly = 0) { repository.updateExpense(any()) }
    }

    @Test
    fun `deleteExpense should delete provided expense and load expenses`() = runTest {
        val expense = Expense("1", "2023-10-07", "test", 10.0)

        viewModel.deleteExpense(expense)

        coVerify {
            repository.deleteExpense(expense)
            repository.expensesFlow
        }
    }

    @Test
    fun `selectExpense should update selected expense`() {
        val expense = Expense("1", "2023-10-07", "test", 10.0)

        viewModel.selectExpense(expense)

        assertEquals(expense, viewModel.state.value.selectedExpense)
    }

    @Test
    fun `clearSelectedExpense should clear selected expense`() {
        val expense = Expense("1", "2023-10-07", "test", 10.0)
        viewModel.selectExpense(expense)

        viewModel.clearSelectedExpense()

        assertNull(viewModel.state.value.selectedExpense)
    }



    @Test
    fun `triggerInputError should update ViewState with given error message`() {
        viewModel.triggerInputError("Test Error")

        assertEquals(ErrorType.TextFieldError("Test Error"), viewModel.state.value.error)
    }

    @Test
    fun `clearError should reset the error state to None`() {
        viewModel.triggerInputError("Test Error")
        viewModel.clearError()

        assertEquals(ErrorType.None, viewModel.state.value.error)
    }

    @Test
    fun `loadExpenses should update ViewState with expenses and set isLoading to false`() = runTest {
        val expenses = listOf(
            Expense("1", "2023-10-07", "test1", 10.0),
            Expense("2", "2023-10-08", "test2", 20.0)
        )
        coEvery { repository.expensesFlow } returns flowOf(expenses)

        viewModel.loadExpenses()

        assertEquals(expenses, viewModel.state.value.expenses)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadExpenses should handle errors and update ViewState accordingly`() = runTest {
        coEvery { repository.expensesFlow } throws Exception("Error loading data")

        viewModel.loadExpenses()

        assertEquals(ErrorType.LazyListError("Failed to load data."), viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `isExpenseDataValid should return false and trigger error for empty expense name`() {
        assertFalse(viewModel.isExpenseDataValid("", "10", "2023-10-07"))
        assertEquals(ErrorType.TextFieldError("Please enter expense name"), viewModel.state.value.error)
    }

    @Test
    fun `isExpenseDataValid should return false and trigger error for short expense name`() {
        assertFalse(viewModel.isExpenseDataValid("a", "10", "2023-10-07"))
        assertEquals(ErrorType.TextFieldError("Please enter a valid expense name"), viewModel.state.value.error)
    }

    @Test
    fun `isExpenseDataValid should return false and trigger error for invalid amount`() {
        assertFalse(viewModel.isExpenseDataValid("test", "-1", "2023-10-07"))
        assertEquals(ErrorType.TextFieldError("Please enter a valid amount"), viewModel.state.value.error)
    }

    @Test
    fun `isExpenseDataValid should return false and trigger error for empty date`() {
        assertFalse(viewModel.isExpenseDataValid("test", "10", ""))
        assertEquals(ErrorType.TextFieldError("Please enter date"), viewModel.state.value.error)
    }

    @Test
    fun `isDataValidForUpdate should return false for all empty fields`() {
        assertFalse(viewModel.isDataValidForUpdate("", "", ""))
    }

    @Test
    fun `getExpenseForUpdate should return expense with updated values when provided`() {
        val currentExpense = Expense("1", "2023-10-07", "test", 10.0)
        val updatedExpense = viewModel.getExpenseForUpdate(currentExpense, "updated", "20.0", "2023-10-08")

        assertEquals("updated", updatedExpense.expenseName)
        assertEquals(20.0, updatedExpense.amount, 0.0)
        assertEquals("2023-10-08", updatedExpense.date)
    }

    @Test
    fun `getExpenseForUpdate should return expense with original values when not provided`() {
        val currentExpense = Expense("1", "2023-10-07", "test", 10.0)
        val updatedExpense = viewModel.getExpenseForUpdate(currentExpense)

        assertEquals(currentExpense.expenseName, updatedExpense.expenseName)
        assertEquals(currentExpense.amount, updatedExpense.amount, 0.0)
        assertEquals(currentExpense.date, updatedExpense.date)
    }

}
