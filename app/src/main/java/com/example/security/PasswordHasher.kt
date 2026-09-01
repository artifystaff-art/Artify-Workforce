package com.example.security

import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2-HMAC-SHA256 password hashing (no plaintext or reversible storage).
 * Encoded as "$pbkdf2-sha256$<iterations>$<saltBase64>$<hashBase64>" so the
 * iteration count and salt travel with the hash and can be verified later
 * even if [ITERATIONS] changes for future accounts.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val PREFIX = "\$pbkdf2-sha256\$"

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val digest = derive(password, salt, ITERATIONS)
        return PREFIX + ITERATIONS + "$" +
            Base64.encodeToString(salt, Base64.NO_WRAP) + "$" +
            Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.removePrefix(PREFIX).split("$")
        if (parts.size != 3) return false
        val iterations = parts[0].toIntOrNull() ?: return false
        val salt = try { Base64.decode(parts[1], Base64.NO_WRAP) } catch (e: IllegalArgumentException) { return false }
        val expected = try { Base64.decode(parts[2], Base64.NO_WRAP) } catch (e: IllegalArgumentException) { return false }
        val actual = derive(password, salt, iterations)
        return constantTimeEquals(actual, expected)
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }
}
