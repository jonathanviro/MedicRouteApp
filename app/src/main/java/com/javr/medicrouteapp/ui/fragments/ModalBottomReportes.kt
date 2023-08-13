package layout.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.firebase.PacienteProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.ui.administrador.VerPdfActivity
import com.javr.medicrouteapp.utils.CustomHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class ModalBottomReportes : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private lateinit var dialogoCarga: AlertDialog
    private lateinit var btnReporteMedicos: CardView
    private lateinit var btnReportePacientes: CardView

    private val medicoProvider = MedicoProvider()
    private val pacienteProvider = PacienteProvider()
    private val historialProvider = HistorialProvider()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_reportes, container, false)

        dialogoCarga = Global.dialogoCarga(requireContext(), "Espere un momento")

        btnReporteMedicos = view.findViewById(R.id.btnReporteMedicos)
        btnReportePacientes = view.findViewById(R.id.btnReportePacientes)

        btnReporteMedicos.setOnClickListener {
            dialogoCarga.show()
            btnReporteMedicos.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transaparent))
            generarReporteMedicos()
        }
        btnReportePacientes.setOnClickListener {
            dialogoCarga.show()
            btnReportePacientes.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transaparent))
            generarReportePacientes()
        }

        return view
    }

    private fun generarReporteMedicos() {
        medicoProvider.getAllMedicos().get().addOnCompleteListener { query ->
            if (query.isSuccessful) {
                val medicosList = mutableListOf<Medico>()

                for (document in query.result!!) {
                    val medico = document.toObject(Medico::class.java)
                    medicosList.add(medico)
                }

                // Usar corutinas para calcular las calificaciones
                CoroutineScope(Dispatchers.Main).launch {
                    for (medico in medicosList) {
                        val querySnapshot = historialProvider.getAllHistorialByMedico(medico.id!!).get().await()
                        var totalCalificaciones = 0f
                        var totalNumCalificaciones = 0
                        var calificacionMedico = 0f

                        if (querySnapshot != null) {
                            if (querySnapshot.documents.size > 0) {
                                for (document in querySnapshot.documents) {
                                    val historial = document.toObject(Historial::class.java)
                                    if (historial != null) {
                                        totalCalificaciones += historial.calificacionParaMedico!!
                                        totalNumCalificaciones++
                                    }
                                }

                                if (totalNumCalificaciones > 0) {
                                    calificacionMedico = totalCalificaciones / totalNumCalificaciones.toFloat()
                                }
                            }
                        }

                        medico.calificacionGeneral = "%.2f".format(calificacionMedico).toFloat()
                    }

                    // Una vez calculadas las calificaciones, imprimir la lista
                    Log.d("FIRESTORE", "LISTA MEDICOS $medicosList")
                    crearPdfMedicos(medicosList)
                }
            }
        }
    }

    private fun crearPdfMedicos(medicosList: List<Medico>) {
        try {
            val carpeta = "/medicroutePdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
//                Toast.makeText(this, "CARPETA CREADA", Toast.LENGTH_SHORT).show()
            }

            val file = File(dir, "reporte_medicos.pdf")
            val fileOutputStream = FileOutputStream(file)

            val documento = Document(PageSize.A4, 20F, 20F, 25F, 25F)
            val writer = PdfWriter.getInstance(documento, fileOutputStream)

            // Agregar el evento personalizado para el encabezado
            val titulo = "CALIFICACIONES DE MÉDICOS" // Puedes cambiar el título aquí
            val event = CustomHeader(requireContext(), titulo)
            writer.pageEvent = event

            documento.open()

            //FUENTES
            val fuenteTituloNivel3 = FontFactory.getFont("arial", 10f, Font.BOLD, BaseColor.BLACK)
            val fuenteNormal = FontFactory.getFont("arial", 8f, BaseColor.BLACK)

            // Agregar parrafo vacio para espacio entre encabezado y tabla
            val  parrafoVacio = Paragraph("\n\n", fuenteNormal)
            documento.add(parrafoVacio)

            // Agregar la tabla
            val columnWidth = floatArrayOf(155F, 155F, 170F, 80F)
            val tabla1 = PdfPTable(columnWidth)
            tabla1.widthPercentage = 100F
            tabla1.spacingBefore = 70f // Espacio entre el título y la tabla

            //FILA 1
            tabla1.addCell(PdfPCell(Paragraph("NOMBRES", fuenteTituloNivel3))).border = 1
            tabla1.addCell(PdfPCell(Paragraph("RAZÓN SOCIAL", fuenteTituloNivel3))).border = 1
            tabla1.addCell(PdfPCell(Paragraph("REGISTRO SANITARIO", fuenteTituloNivel3))).border = 1
            tabla1.addCell(PdfPCell(Paragraph("CALIFICACIÓN", fuenteTituloNivel3))).border = 1

            for(i in 1..50){
                for(medico: Medico in medicosList){
                    if(medico != null){
                        //FILA
                        tabla1.addCell(PdfPCell(Paragraph("${medico.apellidos} ${medico.nombres}", fuenteNormal))).border = 1
                        tabla1.addCell(PdfPCell(Paragraph("${medico.razonSocial}", fuenteNormal))).border = 1
                        tabla1.addCell(PdfPCell(Paragraph("${medico.registroSanitario}", fuenteNormal))).border = 1
                        tabla1.addCell(PdfPCell(Paragraph("${medico.calificacionGeneral}", fuenteNormal))).border = 1
                    }
                }
            }


            documento.add(tabla1)

            documento.close()
            goToPdfViewer(file.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: DocumentException) {
            e.printStackTrace()
        }
    }

    private fun generarReportePacientes() {
        pacienteProvider.getAllPacientes().get().addOnCompleteListener { query ->
            if (query.isSuccessful) {
                val pacientesList = mutableListOf<Paciente>()

                for (document in query.result!!) {
                    val paciente = document.toObject(Paciente::class.java)
                    pacientesList.add(paciente)
                }

                // Usar corutinas para calcular las calificaciones
                CoroutineScope(Dispatchers.Main).launch {
                    for (paciente in pacientesList) {
                        val querySnapshot = historialProvider.getAllHistorialByPaciente(paciente.id!!).get().await()
                        var totalCalificaciones = 0f
                        var totalNumCalificaciones = 0
                        var calificacionMedico = 0f

                        if (querySnapshot != null) {
                            if (querySnapshot.documents.size > 0) {
                                for (document in querySnapshot.documents) {
                                    val historial = document.toObject(Historial::class.java)
                                    if (historial != null) {
                                        totalCalificaciones += historial.calificacionParaPaciente!!
                                        totalNumCalificaciones++
                                    }
                                }

                                if (totalNumCalificaciones > 0) {
                                    calificacionMedico = totalCalificaciones / totalNumCalificaciones.toFloat()
                                }
                            }
                        }

                        paciente.calificacionGeneral = "%.2f".format(calificacionMedico).toFloat()
                    }

                    // Una vez calculadas las calificaciones, imprimir la lista
                    Log.d("FIRESTORE", "LISTA PACIENTES $pacientesList")
                    crearPdfPacientes(pacientesList)
                }
            }
        }
    }

    private fun crearPdfPacientes(pacienteList: List<Paciente>) {
        try {
            val carpeta = "/medicroutePdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
//                Toast.makeText(this, "CARPETA CREADA", Toast.LENGTH_SHORT).show()
            }

            val file = File(dir, "reporte_pacientes.pdf")
            val fileOutputStream = FileOutputStream(file)

            val documento = Document(PageSize.A4, 20F, 20F, 25F, 25F)
            val writer = PdfWriter.getInstance(documento, fileOutputStream)

            // Agregar el evento personalizado para el encabezado
            val titulo = "CALIFICACIONES DE PACIENTES" // Puedes cambiar el título aquí
            val event = CustomHeader(requireContext(), titulo)
            writer.pageEvent = event

            documento.open()

            //FUENTES
            val fuenteTituloNivel3 = FontFactory.getFont("arial", 10f, Font.BOLD, BaseColor.BLACK)
            val fuenteNormal = FontFactory.getFont("arial", 8f, BaseColor.BLACK)

            // Agregar parrafo vacio para espacio entre encabezado y tabla
            val  parrafoVacio = Paragraph("\n\n", fuenteNormal)
            documento.add(parrafoVacio)

            // Agregar la tabla
            val columnWidth = floatArrayOf(155F, 155F, 170F, 80F)
            val tabla1 = PdfPTable(columnWidth)
            tabla1.widthPercentage = 100F
            tabla1.spacingBefore = 70f // Espacio entre el título y la tabla

            //FILA 1
            tabla1.addCell(PdfPCell(Paragraph("NOMBRES", fuenteTituloNivel3))).border = 1
            tabla1.addCell(PdfPCell(Paragraph("CÉDULA", fuenteTituloNivel3))).border = 1
            tabla1.addCell(PdfPCell(Paragraph("GÉNERO", fuenteTituloNivel3))).border = 1
            tabla1.addCell(PdfPCell(Paragraph("CALIFICACIÓN", fuenteTituloNivel3))).border = 1

            for(medico: Paciente in pacienteList){
                if(medico != null){
                    //FILA
                    tabla1.addCell(PdfPCell(Paragraph("${medico.apellidos} ${medico.nombres}", fuenteNormal))).border = 1
                    tabla1.addCell(PdfPCell(Paragraph("${medico.cedula}", fuenteNormal))).border = 1
                    tabla1.addCell(PdfPCell(Paragraph("${medico.sexo}", fuenteNormal))).border = 1
                    tabla1.addCell(PdfPCell(Paragraph("${medico.calificacionGeneral}", fuenteNormal))).border = 1
                }
            }


            documento.add(tabla1)

            documento.close()
            goToPdfViewer(file.absolutePath)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: DocumentException) {
            e.printStackTrace()
        }
    }

    private fun goToPdfViewer(absolutePath: String) {
        dialogoCarga.dismiss()
        val intent = Intent(requireContext(), VerPdfActivity::class.java)
        intent.putExtra(VerPdfActivity.EXTRA_PATH_PDF, absolutePath)
        startActivity(intent)
        dismiss()
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}