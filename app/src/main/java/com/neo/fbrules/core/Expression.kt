package com.neo.fbrules.core

import java.util.regex.Pattern

object Expression {
    val variableInProperty: Pattern = Pattern.compile("((?<=/)|^)\\$\\w+(?=/?)$")
    val variableInCondition: Pattern = Pattern.compile("\\$\\w+(?=/?)$")
}