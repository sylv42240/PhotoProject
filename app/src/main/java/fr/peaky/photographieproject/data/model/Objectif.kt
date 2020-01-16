package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class Objectif(
    var id: String = "",
    val userId: String = "",
    val name: String = ""
): Serializable