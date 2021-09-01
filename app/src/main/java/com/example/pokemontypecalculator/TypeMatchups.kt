package com.example.pokemontypecalculator

import java.util.*

class TypeMatchups(
    val Bug: Map<String, Double>, //Statically typed, dynamically valued
    val Dark: Map<String, Double>,
    val Dragon: Map<String, Double>,
    val Electric: Map<String, Double>,
    val Fairy: Map<String, Double>,
    val Fighting: Map<String, Double>,
    val Fire: Map<String, Double>,
    val Flying: Map<String, Double>,
    val Ghost: Map<String, Double>,
    val Grass: Map<String, Double>,
    val Ground: Map<String, Double>,
    val Ice: Map<String, Double>,
    val Normal: Map<String, Double>,
    val Poison: Map<String, Double>,
    val Psychic: Map<String, Double>,
    val Rock: Map<String, Double>,
    val Steel: Map<String, Double>,
    val Water: Map<String, Double>
)

enum class PokemonType(val type: String) {
    BUG("Bug"), // Statically typed, statically valued
    DARK("Dark"),
    DRAGON("Dragon"),
    ELECTRIC("Electric"),
    FAIRY("Fairy"),
    FIGHTING("Fighting"),
    FIRE("Fire"),
    FLYING("Flying"),
    GHOST("Ghost"),
    GRASS("Grass"),
    GROUND("Ground"),
    ICE("Ice"),
    NORMAL("Normal"),
    POISON("Poison"),
    PSYCHIC("Psychic"),
    ROCK("Rock"),
    STEEL("Steel"),
    WATER("Water")
}

enum class Effectiveness (val impact: String) {
    DNE("DNE"),
    UNVE("UNVE"),
    NVE("NVE"),
    E("E"),
    SE("SE"),
    USE("USE"),
    UDNE("UDNE")
}