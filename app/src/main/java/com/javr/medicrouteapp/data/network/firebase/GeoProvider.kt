package com.javr.medicrouteapp.data.network.firebase

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery

class GeoProvider {
    val collection = FirebaseFirestore.getInstance().collection("LocalizacionesMedicosConectados")
    val geoFirestore = GeoFirestore(collection)

    val collectionWorking = FirebaseFirestore.getInstance().collection("LocalizacionesWorking")
    val geoFirestoreWorking = GeoFirestore(collectionWorking)

    fun saveLocation(idMedico: String, position: LatLng) {
        geoFirestore.setLocation(idMedico, GeoPoint(position.latitude, position.longitude))
    }

    fun removeLocation(idMedico: String) {
        collection.document(idMedico).delete()
    }

    fun getLocation(idMedico: String): Task<DocumentSnapshot> {
        return collection.document(idMedico).get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "ERROR: ${exception.toString()}")
        }
    }

    fun getMedicosCercanos(position: LatLng, radius: Double): GeoQuery {
        val query = geoFirestore.queryAtLocation(GeoPoint(position.latitude, position.longitude), radius)
        query.removeAllListeners()
        return query
    }

    //LOCALIZACION DE CONSULTAS
    fun saveLocationWorking(idPaciente: String, position: LatLng) {
        geoFirestoreWorking.setLocation(idPaciente, GeoPoint(position.latitude, position.longitude))
    }

    fun getLocationWorking(idMedico: String): DocumentReference {
        return collectionWorking.document(idMedico)
    }

    fun removeLocationWorking(idMedico: String) {
        collectionWorking.document(idMedico).delete()
    }
}