package com.javr.medicrouteapp.data.network.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdministradorProvider {
    private val db = Firebase.firestore.collection("Administradores")
    private val authProvider = AuthProvider()

    fun getAdministrador(): DocumentReference {
        return db.document(authProvider.getId())
    }
}