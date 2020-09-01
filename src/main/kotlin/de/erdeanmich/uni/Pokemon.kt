package de.erdeanmich.uni

data class Pokemon(
    val id: String,
    val spriteUrls: List<String>,
    val name: String,
    val types: List<String>,
    val total: Int,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int
)
