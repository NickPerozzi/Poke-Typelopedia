package com.example.pokemontypecalculator

import android.graphics.drawable.Drawable

class TypeGridView {

    var iconsInGridView: Int?
    var textInGridView: Double?
    var backgroundColorInGridView: Int?
    var textColorInGridView: Int?

    constructor(iconsInGridView: Int?, textInGridView: Double?, backgroundColorInGridView: Int?, textColorInGridView: Int?) {
        this.iconsInGridView = iconsInGridView
        this.textInGridView = textInGridView
        this.backgroundColorInGridView = backgroundColorInGridView
        this.textColorInGridView = textColorInGridView
    }

}