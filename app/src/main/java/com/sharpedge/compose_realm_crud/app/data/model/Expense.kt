package com.sharpedge.compose_realm_crud.app.data.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

open class Expense() : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var date: String = ""
    var expenseName: String = ""
    var amount: Double = 0.0

    constructor(
        id: String = UUID.randomUUID().toString(),
        date: String,
        expenseName: String,
        amount: Double
    ) : this() {
        this.id = id
        this.date = date
        this.expenseName = expenseName
        this.amount = amount
    }


    fun copy(): Expense {
        return Expense(id, date, expenseName, amount)
    }


}