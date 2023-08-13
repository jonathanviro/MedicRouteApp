package com.javr.medicrouteapp.ui.menus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.Flag
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.databinding.ActivityOpcionesAdministradorBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.ui.administrador.MedicosActivosActivity
import com.javr.medicrouteapp.ui.administrador.MedicosPendientesActivity
import layout.fragments.ModalBottomReportes

class OpcionesAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpcionesAdministradorBinding
    private val modalSolicitud = ModalBottomReportes()
    private val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        Toolbar().showToolbar(this, "Opciones Administrador", false)
    }

    private fun initListener() {
        binding.cvMedicosPendientes.setOnClickListener {
            val intent = Intent(this, MedicosPendientesActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.cvMedicosActivos.setOnClickListener {
            val intent = Intent(this, MedicosActivosActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.cvReportes.setOnClickListener {
            showModalSolicitud()
        }
    }

    private fun showModalSolicitud() {
        val bundle = Bundle()
        modalSolicitud.arguments = bundle
        modalSolicitud.show(supportFragmentManager, ModalBottomReportes.TAG)
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
}