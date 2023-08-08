package com.javr.medicrouteapp.data.network.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javr.medicrouteapp.data.network.model.Historial

class HistorialProvider {
    val  db = Firebase.firestore.collection("HistorialAtenciones")
    val authProvider = AuthProvider()

    fun create(historial: Historial): Task<DocumentReference> {
        return db.add(historial).addOnFailureListener {
            Log.d("FIRESTORE", "EROR ${it.message}")
        }
    }

    fun remove(): Task<Void> {
        return db.document(authProvider.getId()).delete().addOnFailureListener {
            Log.d("FIRESTORE", "EROR ${it.message}")
        }
    }

    fun updateCalificationToClient(id: String, calification: Float): Task<Void>{
        return db.document(id).update("calificationToClient", calification).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR ${it.message}")
        }
    }

    fun updateCalificationToMedico(id: String, calification: Float): Task<Void>{
        return db.document(id).update("calificacionParaMedico", calification).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR ${it.message}")
        }
    }

    fun getLastHistorialByPaciente() : Query{ // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idPaciente", authProvider.getId())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
    }

    fun getLastHistorialByMedico() : Query{ // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idMedico", authProvider.getId())
                 .orderBy("timestamp", Query.Direction.DESCENDING)
                 .limit(1)
    }

    fun getAllHistorialByPaciente(idPaciente: String) : Query{ // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idPaciente", idPaciente)
                 .orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getAllHistorialByMedico(idMedico: String) : Query{ // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idMedico", idMedico)
                  .orderBy("timestamp", Query.Direction.DESCENDING)
    }
}