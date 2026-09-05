package com.melodi.sampahjujur.data.local.converter

import androidx.room.TypeConverter
import com.melodi.sampahjujur.model.ScrapMaterial
import org.json.JSONArray
import org.json.JSONObject

/**
 * Room TypeConverters for Person 4 entities (ScrapMaterial list, etc.).
 */
class CollectionConverters {

    @TypeConverter
    fun fromScrapMaterialList(items: List<ScrapMaterial>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("materialId", item.materialId)
                put("category", item.category)
                put("description", item.description)
                put("approximateWeight", item.approximateWeight)
                put("currentBuyingRate", item.currentBuyingRate)
                put("estimatedValue", item.estimatedValue)
                put("quotedPrice", item.quotedPrice)
                put("photoReference", item.photoReference)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toScrapMaterialList(json: String): List<ScrapMaterial> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<ScrapMaterial>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ScrapMaterial(
                        materialId = obj.optString("materialId", ""),
                        category = obj.optString("category", ""),
                        description = obj.optString("description", ""),
                        approximateWeight = obj.optDouble("approximateWeight", 0.0),
                        currentBuyingRate = obj.optDouble("currentBuyingRate", 0.0),
                        estimatedValue = obj.optDouble("estimatedValue", 0.0),
                        quotedPrice = obj.optDouble("quotedPrice", 0.0),
                        photoReference = obj.optString("photoReference", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list on parse failure
        }
        return list
    }
}
