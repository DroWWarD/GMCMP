package ru.acs.grandmap.core.crypto

import okio.ByteString.Companion.encodeUtf8

/** Hex (lowercase) SHA-256. */
fun sha256Hex(text: String): String =
    text.encodeUtf8().sha256().hex()
