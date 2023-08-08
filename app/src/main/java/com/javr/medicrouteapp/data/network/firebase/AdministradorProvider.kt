package com.javr.medicrouteapp.data.network.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javr.medicrouteapp.data.network.model.Medico

class AdministradorProvider {
    private val db = Firebase.firestore.collection("Administradores")
    private val authProvider = AuthProvider()

    fun getAdministrador(): DocumentReference {
        return db.document(authProvider.getId())
    }
}