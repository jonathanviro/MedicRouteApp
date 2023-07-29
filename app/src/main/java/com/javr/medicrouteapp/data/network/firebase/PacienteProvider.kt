package com.javr.medicrouteapp.data.network.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javr.medicrouteapp.data.network.model.Paciente

class PacienteProvider {
    private val db = Firebase.firestore.collection("Pacientes")
    private val authProvider = AuthProvider()

    fun create(paciente: Paciente): Task<Void>{
        return db.document(paciente.id!!).set(paciente)
    }

    fun getPaciente(): DocumentReference {
        return db.document(authProvider.getId())
    }
}