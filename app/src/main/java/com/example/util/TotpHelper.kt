package com.example.util

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpHelper {
    fun generateTotp(
        secretKey: String,
        timeSeconds: Long = System.currentTimeMillis() / 1000L,
        timeStepSeconds: Long = 30L
    ): String? {
        val cleanSecret = secretKey.replace(" ", "").replace("-", "").uppercase()
        if (cleanSecret.isEmpty()) return null
        val decodedKey = base32Decode(cleanSecret) ?: return null
        return try {
            val timeStep = timeSeconds / timeStepSeconds
            val data = ByteBuffer.allocate(8).putLong(timeStep).array()
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(decodedKey, "HmacSHA1"))
            val hash = mac.doFinal(data)
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)
            val otp = binary % 1000000
            String.format("%06d", otp)
        } catch (e: Exception) {
            null
        }
    }

    fun getRemainingSeconds(timeSeconds: Long = System.currentTimeMillis() / 1000L, timeStepSeconds: Long = 30L): Int {
        val rem = timeStepSeconds - (timeSeconds % timeStepSeconds)
        return rem.toInt()
    }

    private fun base32Decode(base32: String): ByteArray? {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val clean = base32.replace("=", "")
        val bytes = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in clean) {
            val valIndex = base32Chars.indexOf(c)
            if (valIndex < 0) return null
            buffer = (buffer shl 5) or valIndex
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bytes.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return bytes.toByteArray()
    }
}
