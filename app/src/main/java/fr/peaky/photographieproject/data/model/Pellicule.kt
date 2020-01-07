package fr.peaky.photographieproject.data.model

data class Pellicule (
    var id: String = "",
    val userId: String = "",
    val name: String = "",
    val device: Device = Device(),
    val iso: Int = 0,
    val photos: List<Photo> = emptyList()
)