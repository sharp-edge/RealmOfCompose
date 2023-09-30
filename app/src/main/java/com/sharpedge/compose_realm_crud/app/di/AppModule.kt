package com.sharpedge.compose_realm_crud.app.di

import com.sharpedge.compose_realm_crud.app.data.realm.DataSource
import com.sharpedge.compose_realm_crud.app.data.model.Expense
import com.sharpedge.compose_realm_crud.app.data.repository.ExpenseRepository
import com.sharpedge.compose_realm_crud.app.ui.viewmodel.MainViewModel
import io.realm.kotlin.RealmConfiguration
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MainViewModel(get()) }
    single { ExpenseRepository(get()) }
    single {

        RealmConfiguration.Builder(
            schema = setOf(Expense::class)
        )
            .name("sharpedge-test.db")
            .schemaVersion(1)
            .build()

    }

    single { DataSource(get()) }
}