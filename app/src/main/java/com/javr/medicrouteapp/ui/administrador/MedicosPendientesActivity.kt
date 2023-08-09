package com.javr.medicrouteapp.ui.administrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.databinding.ActivityMedicosPendientesBinding
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.ui.adapter.MedicoAdapter
import com.javr.medicrouteapp.ui.menus.OpcionesAdministradorActivity
import com.javr.medicrouteapp.utils.MyToolbar

class MedicosPendientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicosPendientesBinding
    lateinit var medicoAdapter: MedicoAdapter
    private val lstMedicos = mutableListOf<Medico>()

    private val authProvider = AuthProvider()
    private var medicoProvider = MedicoProvider()
    private var medicosPendientesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicosPendientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Médicos Pendientes", false)

        listarMedicosPendientesConfirmacion()
        initRecyclerView()
    }

    private fun listarMedicosPendientesConfirmacion() {
        medicosPendientesListener = medicoProvider.getSolicitudesByStatus("pendiente").addSnapshotListener { snapshot, e ->
            var medicos = emptyList<Medico>()
            if (e != null) {
                Log.d("FIRESTORE", "ERROR ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null)  {
                if (snapshot.documents.size > 0) {
                    medicos = snapshot.toObjects(Medico::class.java)
                }
            }

            lstMedicos.clear()
            lstMedicos.addAll(medicos)
            medicoAdapter.notifyDataSetChanged()
        }
    }

    private fun initRecyclerView() {
        medicoAdapter = MedicoAdapter(lstMedicos) { medico ->
            onSelectedItem(medico)
        }

        binding.rvConfirmaciones.layoutManager = LinearLayoutManager(this)
        binding.rvConfirmaciones.adapter = medicoAdapter
    }

    fun onSelectedItem(medico: Medico) {
        val intent = Intent(this, DetailMedicoActivity::class.java)
        intent.putExtra(DetailMedicoActivity.EXTRA_MEDICO, medico)
        startActivity(intent)
        finish()
    }

    private fun goToMain() {
        authProvider.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            goToMain()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        medicosPendientesListener?.remove()
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        startActivity(Intent(this, OpcionesAdministradorActivity::class.java))
        finish()
    }
}