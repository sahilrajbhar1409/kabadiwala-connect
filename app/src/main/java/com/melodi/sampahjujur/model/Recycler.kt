package com.melodi.sampahjujur.model

import com.google.firebase.firestore.PropertyName

/**
 * Recycler profile model (Owned by Person 2; consumed by Person 4).
 * Represents an authorized recycling facility in the formal chain.
 */
data class Recycler(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("businessName") @set:PropertyName("businessName")
    var businessName: String = "",

    @get:PropertyName("facilityLocation") @set:PropertyName("facilityLocation")
    var facilityLocation: String = "",

    @get:PropertyName("latitude") @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude") @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:PropertyName("contactPhone") @set:PropertyName("contactPhone")
    var contactPhone: String = "",

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("authorizationStatus") @set:PropertyName("authorizationStatus")
    var authorizationStatus: String = "AUTHORIZED", // AUTHORIZED, PENDING, REJECTED

    @get:PropertyName("licenseNumber") @set:PropertyName("licenseNumber")
    var licenseNumber: String = "",

    @get:PropertyName("acceptedMaterials") @set:PropertyName("acceptedMaterials")
    var acceptedMaterials: List<String> = emptyList(),

    @get:PropertyName("offeredRatePerKg") @set:PropertyName("offeredRatePerKg")
    var offeredRatePerKg: Double = 0.0,

    @get:PropertyName("pickupAvailable") @set:PropertyName("pickupAvailable")
    var pickupAvailable: Boolean = true,

    @get:PropertyName("serviceArea") @set:PropertyName("serviceArea")
    var serviceArea: String = "",

    @get:PropertyName("upiId") @set:PropertyName("upiId")
    var upiId: String = "recycler@upi"
) {
    fun isAuthorized(): Boolean = authorizationStatus.equals("AUTHORIZED", ignoreCase = true)

    companion object {
        /**
         * Clean fallback list of authorized recyclers for Person 4 flow
         * until Person 2's backend service is populated.
         */
        fun getDefaultAuthorizedRecyclers(): List<Recycler> = listOf(
            Recycler(
                id = "REC-MUM-001",
                name = "EcoRecycle Tech Ltd.",
                businessName = "EcoRecycle Authorized Hub",
                facilityLocation = "Plot 42, MIDC Industrial Area, Mumbai",
                latitude = 19.1136,
                longitude = 72.8697,
                contactPhone = "+91 98201 12345",
                email = "contact@ecorecycle.in",
                authorizationStatus = "AUTHORIZED",
                licenseNumber = "MPCB/E-WASTE/2026/089",
                acceptedMaterials = listOf("CRT", "LCD", "PCB", "Batteries", "Motors"),
                offeredRatePerKg = 42.0,
                pickupAvailable = true,
                serviceArea = "Mumbai Metropolitan Region",
                upiId = "ecorecycle@oksbi"
            ),
            Recycler(
                id = "REC-PUN-002",
                name = "GreenE-Waste Solutions",
                businessName = "GreenE Formal Recycling Depot",
                facilityLocation = "Sector 10, Bhosari Industrial Zone, Pune",
                latitude = 18.6298,
                longitude = 73.8567,
                contactPhone = "+91 98500 54321",
                email = "operations@greenewaste.com",
                authorizationStatus = "AUTHORIZED",
                licenseNumber = "MPCB/E-WASTE/2025/112",
                acceptedMaterials = listOf("Cables", "Mixed plastics", "Magnets", "PCB"),
                offeredRatePerKg = 38.5,
                pickupAvailable = true,
                serviceArea = "Pune & Pimpri-Chinchwad",
                upiId = "greenewaste@icici"
            ),
            Recycler(
                id = "REC-MAH-003",
                name = "Sahyadri Circular Processors",
                businessName = "Sahyadri Formal E-Waste Processing Plant",
                facilityLocation = "Gate 7, Ranjangaon Industrial Area, Pune",
                latitude = 18.7753,
                longitude = 74.2435,
                contactPhone = "+91 98811 99887",
                email = "info@sahyadricircular.org",
                authorizationStatus = "AUTHORIZED",
                licenseNumber = "CPCB/EPR/2026/743",
                acceptedMaterials = listOf("CRT", "LCD", "PCB", "Cables", "Batteries", "Motors", "Magnets", "Mixed plastics"),
                offeredRatePerKg = 45.0,
                pickupAvailable = true,
                serviceArea = "Maharashtra State",
                upiId = "sahyadricircular@upi"
            )
        )
    }
}
