package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class Sequence(
    var id: String = "",
    val pelliculeId: String = "",
    val name: String = "",
    val appareilId: String = ""
): Serializable