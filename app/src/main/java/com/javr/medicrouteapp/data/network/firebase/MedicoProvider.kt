package com.javr.medicrouteapp.data.network.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javr.medicrouteapp.data.network.model.Medico

class MedicoProvider {
    private val db = Firebase.firestore.collection("Medicos")
    private val authProvider = AuthProvider()

    fun create(medico: Medico): Task<Void>{
        return db.document(medico.id!!).set(medico)
    }
    fun getMedico(): DocumentReference {
        return db.document(authProvider.getId())
    }

}