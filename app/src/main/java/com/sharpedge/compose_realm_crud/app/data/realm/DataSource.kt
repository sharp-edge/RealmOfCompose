package com.sharpedge.compose_realm_crud.app.data.realm

import android.util.Log
import com.sharpedge.compose_realm_crud.app.data.model.Expense
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

class DataSource(private val realmConfiguration: RealmConfiguration) {


    suspend fun insert(expense: Expense) {
        val realm: Realm = Realm.open(realmConfiguration)
        realm.write {
            copyToRealm(expense)
        }
        realm.close()
    }

    suspend fun update(expense: Expense) {
        val realm: Realm = Realm.open(realmConfiguration)
        try {

            realm.write {
                val obj = this.query<Expense>("id == $0", expense.id).first().find()
                obj?.apply {
                    this.amount = expense.amount
                    this.expenseName = expense.expenseName
                    this.date = expense.date
                }
                Log.d("update", ("update() " + obj == null).toString())

            }
        } catch (ex: Exception) {
            Log.e("Sarmad", "Exception in update()", ex)
        } finally {
            realm.close()
        }

    }

    suspend fun delete(expense: Expense) {
        val realm: Realm = Realm.open(realmConfiguration)
        realm.write {
            val item = this.query<Expense>("id == $0", expense.id).find().first()
            delete(item)
        }
        realm.close()
    }

    suspend fun fetchAll(): List<Expense> {
        val realm: Realm = Realm.open(realmConfiguration)
        val obj = realm.query<Expense>().find().toList()
        val listToReturn = obj.map { it.copy() }
        realm.close()
        return listToReturn
    }
}