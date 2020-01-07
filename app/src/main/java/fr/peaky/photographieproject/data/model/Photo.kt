package fr.peaky.photographieproject.data.model

data class Photo(
    var id: String = "",
    val userId: String = "",
    val pelliculeId: String = "",
    val description: String = "",
    val objectif: Objectif = Objectif(),
    val mode: String = "",
    val ouverture: String = "",
    val exposition: String = ""
)