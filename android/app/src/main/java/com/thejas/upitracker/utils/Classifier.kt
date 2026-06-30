package com.thejas.upitracker.utils

import com.thejas.upitracker.data.model.Category
import org.json.JSONArray

object Classifier {

    fun classify(description: String?, vpa: String?, categories: List<Category>): Long? {
        val text = "${description.orEmpty()} ${vpa.orEmpty()}".lowercase()

        for (cat in categories) {
            if (cat.name == "Others") continue
            val keywords = parseKeywords(cat.keywords)
            if (keywords.any { kw -> kw.isNotBlank() && text.contains(kw.lowercase()) }) {
                return cat.id
            }
        }
        return categories.firstOrNull { it.name == "Others" }?.id
    }

    private fun parseKeywords(json: String): List<String> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (_: Exception) { emptyList() }
}
