package com.melodi.sampahjujur.model

import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generator for collision-resistant Lot IDs and Handover References (SIH Person 4).
 * Formats:
 *  - Lot ID: KC-YYYYMMDD-XXXXXX (e.g. KC-20260905-A82F91)
 *  - Handover Reference: HO-KC-YYYYMMDD-XXXXXX (e.g. HO-KC-20260905-F41C90)
 */
object LotIdGenerator {
    private val random = SecureRandom()
    private const val CHARACTERS = "0123456789ABCDEF"

    /**
     * Generates a collision-resistant Lot ID.
     * Follows specified format: KC-YYYYMMDD-XXXXXX
     */
    fun generateLotId(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val suffix = generateRandomHex(6)
        return "KC-$dateStr-$suffix"
    }

    /**
     * Generates a unique Digital Handover Reference.
     * Follows format: HO-KC-YYYYMMDD-XXXXXX
     */
    fun generateHandoverReference(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val suffix = generateRandomHex(6)
        return "HO-KC-$dateStr-$suffix"
    }

    private fun generateRandomHex(length: Int): String {
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            val idx = random.nextInt(CHARACTERS.length)
            sb.append(CHARACTERS[idx])
        }
        return sb.toString()
    }
}
