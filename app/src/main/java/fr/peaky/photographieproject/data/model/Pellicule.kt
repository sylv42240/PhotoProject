package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class Pellicule (
    var id: String = "",
    val userId: String = "",
    val name: String = "",
    val iso: String = "",
    val poses: Int = 0
) : Serializable