package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class Sequence(
    var id: String = "",
    val groupeSequenceId: String = "",
    val name: String = ""
): Serializable