package com.trustengine.verifier.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EKTPData(
    val nik: String,
    val name: String,
    val placeOfBirth: String,
    val dateOfBirth: String,
    val gender: String,
    val bloodType: String = "",
    val address: String,
    val rt: String = "",
    val rw: String = "",
    val village: String = "",
    val district: String = "",
    val religion: String = "",
    val maritalStatus: String = "",
    val occupation: String = "",
    val nationality: String = "WNI",
    val expiryDate: String = "",
    val photoData: ByteArray? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EKTPData

        if (nik != other.nik) return false
        if (name != other.name) return false
        if (placeOfBirth != other.placeOfBirth) return false
        if (dateOfBirth != other.dateOfBirth) return false
        if (gender != other.gender) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nik.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + placeOfBirth.hashCode()
        result = 31 * result + dateOfBirth.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }
}