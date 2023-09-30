package com.sharpedge.compose_realm_crud.app.data.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlin.reflect.KClass
//import io.realm.kotlin.where
//
//fun initRealm(config: RealmConfiguration): Realm {
//    return Realm.open(config)
//}
//
//// Conv
//inline fun <reified T : RealmObject> Realm.fetchAll(): List<T> {
//    val results = this.objects(T::class)
//    return realmListOf(*results.toTypedArray()).toList()
//}
//
//// Insert or update a Realm object
//fun <T : RealmObject> Realm.upsert(obj: T) {
//    // Depending on the SDK version and available APIs,
//    // use appropriate methods to insert or update
//}
//
//// Fetch all objects of a given type
//fun <T : RealmObject> Realm.getAll(clazz: KClass<T>): List<T> {
//    return this.objects(clazz).toList()
//}
//
//// Delete an object from Realm
//fun <T : RealmObject> Realm.delete(obj: T) {
//    // Use appropriate methods to delete an object
//}
