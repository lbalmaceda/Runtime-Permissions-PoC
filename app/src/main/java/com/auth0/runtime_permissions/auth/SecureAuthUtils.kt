package com.auth0.runtime_permissions.auth

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

fun secureRandomString(): String {
    val sr = SecureRandom()
    val randomBytes = ByteArray(32)
    sr.nextBytes(randomBytes)
    return randomBytes.toBase64String()
}

fun ByteArray.toBase64String(): String {
    return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

fun getSHA256(input: ByteArray): ByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(input, 0, input.size)
    return md.digest()
}

fun deriveCodeChallenge(codeVerifier: String): String {
    val input = codeVerifier.toByteArray(StandardCharsets.US_ASCII)
    val signature = getSHA256(input)
    return signature.toBase64String()
}

fun expandQuery(queryOrFragment: String?): Map<String, String> {
    if (queryOrFragment.isNullOrEmpty()) {
        return emptyMap()
    }
    val entries = queryOrFragment.split("&")
    val values: MutableMap<String, String> = mutableMapOf()
    for (entry in entries) {
        val value = entry.split("=")
        if (value.size == 2) {
            values[value[0]] = value[1]
        }
    }
    return values
}