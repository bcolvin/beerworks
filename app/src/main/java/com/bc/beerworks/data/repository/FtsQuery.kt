package com.bc.beerworks.data.repository

/**
 * Sanitizes a raw user search query into prefix-wildcard SQLite FTS4 tokens.
 * Non-alphanumeric characters become token boundaries so hyphenated codes (e.g. "4-VG", "WLP-001")
 * match safely without triggering FTS operator syntax errors.
 */
fun buildFtsQuery(query: String): String {
    val tokens = query
        .replace(Regex("[^a-zA-Z0-9]"), " ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    return tokens.joinToString(" ") { "$it*" }
}
