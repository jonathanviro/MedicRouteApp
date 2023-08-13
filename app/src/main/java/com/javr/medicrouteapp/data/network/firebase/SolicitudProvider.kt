package com.javr.medicrouteapp.data.network.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javr.medicrouteapp.data.network.model.Solicitud

class SolicitudProvider {
    val db = Firebase.firestore.collection("Solicitudes")
    val authProvider = AuthProvider()

    fun create(solicitud: Solicitud): Task<Void> {
        return db.document(authProvider.getId()).set(solicitud).addOnFailureListener {
            Log.d("FIRESTORE", "SolicitudProvider/ERROR ${it.message}")
        }
    }

//    fun updateStatus(idPaciente: String, status: String): Task<Void> {
//        return db.document(idPaciente).update("status", status).addOnFailureListener {
//            Log.d("FIRESTORE", "SolicitudProvider/ERROR ${it.message}")
//        }
//    }

    fun updateStatus(idPaciente: String, status: String, timestamp: Long): Task<Void> {
        val dataToUpdate = hashMapOf<String, Any>(
            "status" to status,
            "timestampActualizacion" to timestamp,
        )

        return db.document(idPaciente).update(dataToUpdate).addOnFailureListener {
            Log.d("FIRESTORE", "SolicitudProvider/ERROR ${it.message}")
        }
    }

    fun remove(): Task<Void> {
        return db.document(authProvider.getId()).delete().addOnFailureListener {
            Log.d("FIRESTORE", "SolicitudProvider/ERROR ${it.message}")
        }
    }

    //  Metodo utilizado para buscar solicitud en pantalla de Medico
    fun getSolicitudByPaciente(): Query {
        return db.whereEqualTo("idPaciente", authProvider.getId())
    }
    //  Metodo utilizado para buscar solicitud en pantalla de Medico
    fun getSolicitudesCreadasForMedico(): Query {
        return db.whereEqualTo("idMedico", authProvider.getId())
    }

    //  Metodo utilizado para escuchar los estados de la solicitud
    fun getEstadoSolicitud(): DocumentReference {
        return db.document(authProvider.getId())
    }

    fun enviarPrecioConsulta(solicitud: Solicitud): Task<Void> {
        val dataToUpdate = hashMapOf<String, Any>(
            "idPaciente" to solicitud.idPaciente!!,
            "status" to solicitud.status!!,
            "nombreMedico" to solicitud.nombreMedico!!,
            "nombreConsultorio" to solicitud.nombreConsultorio!!,
            "precio" to solicitud.precio!!,
            "horaAgendada" to solicitud.horaAgendada!!,
            "consultorioLat" to solicitud.consultorioLat!!,
            "consultorioLng" to solicitud.consultorioLng!!,
            "timestampActualizacion" to solicitud.timestampActualizacion!!
        )

        return db.document(solicitud.idPaciente).update(dataToUpdate).addOnFailureListener {
            Log.d("FIRESTORE", "SolicitudProvider/ERROR ${it.message}")
        }
    }

    // Método para obtener las solicitudes por idMedico y status - // CONSULTA COMPUESTA - INDICE
    fun getSolicitudesByMedicoYStatus(idMedico: String, status: String): Query {
        return db.whereEqualTo("idMedico", idMedico)
                 .whereEqualTo("status", status)
    }

    // Obtener solicitudes aceptadas e iniciadas - // CONSULTA COMPUESTA - INDICE
    fun getSolicitudesByMedicoYStatus(idMedico: String, lstEstados: List<String>): Query {
        return db.whereEqualTo("idMedico", idMedico)
                 .whereIn("status", lstEstados)
                 .orderBy("status")
    }

}