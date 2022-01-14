package com.neo.fbrules.main.presenter.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class RuleModel(
    val path: String = "",
    val conditions : MutableList<RuleCondition> = mutableListOf()
) : Parcelable

@Parcelize
data class RuleCondition(
    val property: String,
    val condition: String
) : Parcelable