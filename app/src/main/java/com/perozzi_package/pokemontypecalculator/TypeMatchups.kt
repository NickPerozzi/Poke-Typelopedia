package com.perozzi_package.pokemontypecalculator

enum class Types(val type: String) {
    NoType("NoType"),
    Bug("Bug"),
    Dark("Dark"),
    Dragon("Dragon"),
    Electric("Electric"),
    Fairy("Fairy"),
    Fighting("Fighting"),
    Fire("Fire"),
    Flying("Flying"),
    Ghost("Ghost"),
    Grass("Grass"),
    Ground("Ground"),
    Ice("Ice"),
    Normal("Normal"),
    Poison("Poison"),
    Psychic("Psychic"),
    Rock("Rock"),
    Steel("Steel"),
    Water("Water")
}

enum class Effectiveness (val impact: String) {
    DOES_NOT_EFFECT("DNE"),
    ULTRA_NOT_VERY_EFFECTIVE("UNVE"),
    NOT_VERY_EFFECTIVE("NVE"),
    EFFECTIVE("E"),
    SUPER_EFFECTIVE("SE"),
    ULTRA_SUPER_EFFECTIVE("USE"),
    ULTRA_DOES_NOT_EFFECT("UDNE")
}