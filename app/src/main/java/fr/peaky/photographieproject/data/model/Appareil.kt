package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class Appareil(
    var id: String = "",
    val userId: String = "",
    val name: String = ""
): Serializable