package com.javr.medicrouteapp.data.network.firebase

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.javr.medicrouteapp.data.network.model.Medico

class MedicoProvider {
    private val db = Firebase.firestore.collection("Medicos")
    private var storageImage = FirebaseStorage.getInstance().getReference().child("Perfiles")
    private var storagePdf = FirebaseStorage.getInstance().getReference().child("PDFs")
    private val authProvider = AuthProvider()

    fun create(medico: Medico): Task<Void>{
        return db.document(medico.id!!).set(medico)
    }

    fun update(medico: Medico): Task<Void> {
        val dataToUpdate = hashMapOf<String, Any>(
            "nombres" to medico.nombres!!,
            "apellidos" to medico.apellidos!!,
            "cedula" to medico.cedula!!,
            "telefono" to medico.telefono!!,
            "sexo" to medico.sexo!!,
            "razonSocial" to medico.razonSocial!!,
            "ruc" to medico.ruc!!,
            "registroSanitario" to medico.registroSanitario!!,
            "consultorioLat" to medico.consultorioLat!!,
            "consultorioLng" to medico.consultorioLng!!,
            "imagenUrl" to medico.imagenUrl!!,
            "pdfUrl" to medico.pdfUrl!!
        )

        return db.document(medico?.id!!).update(dataToUpdate)
    }

    fun getMedico(): DocumentReference {
        return db.document(authProvider.getId())
    }

    fun getMedicoRealTime(): Query {
        return db.whereEqualTo("id", authProvider.getId())
    }

    fun getSolicitudesByStatus(status: String): Query {
        return db.whereEqualTo("status", status)
    }

    fun updateStatus(idMedico: String, status: String): Task<Void> {
        return db.document(idMedico).update("status", status).addOnFailureListener {
            Log.d("FIRESTORE", "MedicoProvider/ERROR ${it.message}")
        }
    }

    fun uploadImagen(id: String, uri: Uri): StorageTask<UploadTask.TaskSnapshot> {
        val ref = storageImage.child("$id.jpg")
        val uploadTask = ref.putFile(uri)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "MedicoProvider/UPLOAD_IMAGE_ERROR ${it.message}")
        }
    }

    fun getImagenUrl(id: String): Task<Uri> {
        val ref = storageImage.child("$id.jpg")
        return ref.downloadUrl
    }

    fun uploadPdf(id: String, uri: Uri): StorageTask<UploadTask.TaskSnapshot> {
        val ref = storagePdf.child("$id.pdf")
        val uploadTask = ref.putFile(uri)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "MedicoProvider/UPLOAD_PDF_ERROR: ${it.message}")
        }
    }

    fun getPdfUrl(id: String): Task<Uri> {
        val ref = storagePdf.child("$id.pdf")
        return ref.downloadUrl
    }

    fun getAllMedicos(): Query {
        return db.orderBy("apellidos")
    }
}