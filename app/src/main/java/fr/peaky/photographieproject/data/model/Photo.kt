package fr.peaky.photographieproject.data.model

import java.io.Serializable

data class Photo(
    var id: String = "",
    val sequenceId: String = "",
    val objectifId: String = "",
    val mode: String = "",  //  Portrait
    val ouverture: String = "", //  F5.6
    val exposition: String = "", //  1/125
    val description: String = "",
    val time: String = "",
    val imagePath: String = "",
    val numberPhoto: Int = 0 //  Numero pellicule
): Serializable