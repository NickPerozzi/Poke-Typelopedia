package com.perozzi_package.pokemontypecalculator

enum class Effectiveness (val impact: String) {
    DOES_NOT_EFFECT("DNE"),
    ULTRA_NOT_VERY_EFFECTIVE("UNVE"),
    NOT_VERY_EFFECTIVE("NVE"),
    EFFECTIVE("E"),
    SUPER_EFFECTIVE("SE"),
    ULTRA_SUPER_EFFECTIVE("USE"),
    ULTRA_DOES_NOT_EFFECT("UDNE")
}