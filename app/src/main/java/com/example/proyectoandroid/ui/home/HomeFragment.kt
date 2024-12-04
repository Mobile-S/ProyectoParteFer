package com.example.proyectoandroid.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyectoandroid.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val investigaciones = mutableListOf<String>()
    private val investigacionesIds = mutableListOf<String>() // Lista para almacenar los ID de las investigaciones

    // Opciones de área definidas en el código
    private val areaOptions = listOf(
        "Seleccione el área",
        "Matematicas",
        "Biologia",
        "Area Social",
        "Etica",
        "Psicologia",
        "fisica",
        "informatica",
        "Arte"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        configureSpinners()
        configureSearchButton()
        loadAllInvestigations()

        return binding.root
    }

    private fun configureSpinners() {
        // Configurar opciones directamente desde el array en el código
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, areaOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.researchAreaSpinner.adapter = adapter
    }

    private fun configureSearchButton() {
        // Configuración del botón "Buscar"
        binding.searchButton.setOnClickListener {
            val selectedArea = binding.researchAreaSpinner.selectedItem.toString()

            if (selectedArea.isNotEmpty() && selectedArea != "Seleccione el área") {
                filterInvestigations(selectedArea)
            } else {
                Toast.makeText(requireContext(), "Seleccione un área de estudio.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAllInvestigations() {
        // Cargar todas las investigaciones al iniciar
        db.collection("WorkData")
            .get()
            .addOnSuccessListener { result ->
                investigaciones.clear()
                investigacionesIds.clear()
                for (document in result) {
                    val title = document.getString("title") ?: "Sin título"
                    val description = document.getString("description") ?: "Sin descripción"
                    val area = document.getString("area") ?: "Sin área"
                    val grado = document.getString("grado") ?: "Sin grado"
                    val idemp = document.id // Obtener el ID del documento
                    investigaciones.add("Título: $title\nDescripción: $description\nÁrea: $area\nGrado: $grado")
                    investigacionesIds.add(idemp) // Guardar el ID en la lista
                }
                updateListView()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterInvestigations(area: String) {
        // Filtrar investigaciones solo por el área seleccionada
        db.collection("WorkData")
            .whereEqualTo("area", area)
            .get()
            .addOnSuccessListener { result ->
                investigaciones.clear()
                investigacionesIds.clear()
                for (document in result) {
                    val title = document.getString("title") ?: "Sin título"
                    val description = document.getString("description") ?: "Sin descripción"
                    val grado = document.getString("grado") ?: "Sin grado"
                    val idemp = document.id // Obtener el ID del documento
                    investigaciones.add("Título: $title\nDescripción: $description\nÁrea: $area\nGrado: $grado")
                    investigacionesIds.add(idemp) // Guardar el ID en la lista
                }
                updateListView()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al filtrar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateListView() {
        // Actualizar el ListView con las investigaciones filtradas
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            investigaciones
        )
        binding.listView.adapter = adapter
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = investigaciones[position]
            val selectedIdemp = investigacionesIds[position] // Obtener el ID correspondiente
            Toast.makeText(
                context,
                "Id investigación: $selectedIdemp\n$selectedItem",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}