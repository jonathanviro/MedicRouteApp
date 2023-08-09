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
import com.javr.medicrouteapp.data.network.model.Paciente
import java.io.File

class PacienteProvider {
    private val db = Firebase.firestore.collection("Pacientes")
    private var storageImage = FirebaseStorage.getInstance().getReference().child("Perfiles")
    private val authProvider = AuthProvider()

    fun create(paciente: Paciente): Task<Void>{
        return db.document(paciente.id!!).set(paciente)
    }

    fun update(paciente: Paciente): Task<Void> {
        val dataToUpdate = hashMapOf<String, Any>(
            "nombres" to paciente.nombres!!,
            "apellidos" to paciente.apellidos!!,
            "cedula" to paciente.cedula!!,
            "telefono" to paciente.telefono!!,
            "sexo" to paciente.sexo!!,
            "imagenUrl" to paciente.imagenUrl!!
        )

        return db.document(paciente?.id!!).update(dataToUpdate)
    }

    fun getPaciente(): DocumentReference {
        return db.document(authProvider.getId())
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

    fun getAllPacientes(): Query {
        return db.orderBy("apellidos")
    }
}