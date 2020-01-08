package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class GroupeSequence(
    var id: String = "",
    val pelliculeId: String = "",
    val appareilId: String = "",
    val name: String = ""
): Serializable