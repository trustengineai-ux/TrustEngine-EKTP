package com.trustengine.verifier.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.trustengine.verifier.domain.model.EKTPData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class NFCEKTPReader @Inject constructor() {
    
    companion object {
        private const val TAG = "NFCEKTPReader"
        private const val SELECT_APDU = "00A4040007A0000002471001"
        private const val READ_DATA_APDU = "00B0000000"
        private const val READ_PHOTO_APDU = "00B0010000"
    }
    
    private var currentTag: Tag? = null
    private var isoDep: IsoDep? = null
    
    fun isNFCSupported(adapter: NfcAdapter?): Boolean {
        return adapter != null
    }
    
    fun isNFCEnabled(adapter: NfcAdapter?): Boolean {
        return adapter?.isEnabled == true
    }
    
    fun setTag(tag: Tag?) {
        currentTag = tag
        isoDep = tag?.let { IsoDep.get(it) }
    }
    
    suspend fun readEKTPData(): Result<EKTPData> = withContext(Dispatchers.IO) {
        try {
            val isoDep = isoDep ?: return@withContext Result.failure(
                IOException("No NFC tag connected")
            )
            
            isoDep.connect()
            isoDep.timeout = 5000
            
            // Select e-KTP application
            val selectResponse = isoDep.transceive(hexToBytes(SELECT_APDU))
            if (!isSuccessResponse(selectResponse)) {
                return@withContext Result.failure(
                    IOException("Failed to select e-KTP application: ${bytesToHex(selectResponse)}")
                )
            }
            
            // Read identity data
            val identityData = isoDep.transceive(hexToBytes(READ_DATA_APDU))
            if (!isSuccessResponse(identityData)) {
                return@withContext Result.failure(
                    IOException("Failed to read identity data: ${bytesToHex(identityData)}")
                )
            }
            
            // Try to read photo data
            val photoData = try {
                isoDep.transceive(hexToBytes(READ_PHOTO_APDU))
            } catch (e: Exception) {
                null
            }
            
            isoDep.close()
            
            // Parse the data
            val ektpData = parseEKTPData(identityData, photoData)
            Result.success(ektpData)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error reading e-KTP", e)
            Result.failure(e)
        }
    }
    
    private fun parseEKTPData(data: ByteArray, photoData: ByteArray?): EKTPData {
        // Parse TLV (Tag-Length-Value) format
        // This is a simplified parser - actual implementation would need to handle
        // the specific e-KTP data format
        
        val textData = String(data, Charsets.UTF_8)
        
        // Extract fields using simple parsing
        // In production, this should use proper TLV parsing
        return EKTPData(
            nik = extractField(textData, "NIK") ?: "",
            name = extractField(textData, "NAMA") ?: "",
            placeOfBirth = extractField(textData, "TEMPAT_LAHIR") ?: "",
            dateOfBirth = extractField(textData, "TANGGAL_LAHIR") ?: "",
            gender = extractField(textData, "JENIS_KELAMIN") ?: "",
            bloodType = extractField(textData, "GOL_DARAH") ?: "",
            address = extractField(textData, "ALAMAT") ?: "",
            rt = extractField(textData, "RT") ?: "",
            rw = extractField(textData, "RW") ?: "",
            village = extractField(textData, "KELURAHAN") ?: "",
            district = extractField(textData, "KECAMATAN") ?: "",
            religion = extractField(textData, "AGAMA") ?: "",
            maritalStatus = extractField(textData, "STATUS_KAWIN") ?: "",
            occupation = extractField(textData, "PEKERJAAN") ?: "",
            nationality = extractField(textData, "KEWARGANEGARAAN") ?: "WNI",
            expiryDate = extractField(textData, "BERLAKU_HINGGA") ?: "",
            photoData = photoData?.takeIf { it.size > 2 }
        )
    }
    
    private fun extractField(data: String, fieldName: String): String? {
        val pattern = "$fieldName[:=]\\s*([^|\\n]+)".toRegex(RegexOption.IGNORE_CASE)
        return pattern.find(data)?.groupValues?.get(1)?.trim()
    }
    
    private fun isSuccessResponse(response: ByteArray): Boolean {
        return response.size >= 2 && 
               response[response.size - 2] == 0x90.toByte() && 
               response[response.size - 1] == 0x00.toByte()
    }
    
    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + 
                          Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
    
    fun close() {
        try {
            isoDep?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing NFC connection", e)
        }
        currentTag = null
        isoDep = null
    }
}