package com.phpls.phpstorm

import com.google.gson.JsonParser
import java.io.InputStream

/**
 * Wraps the LSP server's output stream and sanitizes messages before they reach LSP4IJ:
 *
 * - Strips markdown tables from hover responses to prevent a Swing JEditorPane crash
 *   when rendering HTML tables (TableView$RowView.layoutMajorAxis).
 * - Drops `workspace/inlineValue/refresh` requests, which lsp4j dispatches via
 *   MethodHandles.unreflectSpecial, bypassing any LanguageClient override and throwing
 *   UnsupportedOperationException. Transformed to a no-op notification instead.
 */
class HoverSanitizingInputStream(private val source: InputStream) : InputStream() {
    private var buffer = ByteArray(0)
    private var pos = 0

    override fun read(): Int {
        if (pos >= buffer.size) {
            buffer = readNextMessage() ?: return -1
            pos = 0
        }
        return if (pos < buffer.size) buffer[pos++].toInt() and 0xFF else -1
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        if (pos >= buffer.size) {
            buffer = readNextMessage() ?: return -1
            pos = 0
        }
        val count = minOf(len, buffer.size - pos)
        System.arraycopy(buffer, pos, b, off, count)
        pos += count
        return count
    }

    private fun readNextMessage(): ByteArray? {
        val headerBytes = readUntilDoubleNewline() ?: return null
        val contentLength = headerBytes.toString(Charsets.US_ASCII)
            .lines()
            .find { it.startsWith("Content-Length:") }
            ?.substringAfter(":")
            ?.trim()
            ?.toIntOrNull() ?: return headerBytes

        val body = ByteArray(contentLength)
        var totalRead = 0
        while (totalRead < contentLength) {
            val n = source.read(body, totalRead, contentLength - totalRead)
            if (n < 0) break
            totalRead += n
        }

        val sanitized = sanitizeMessage(body.toString(Charsets.UTF_8)).toByteArray(Charsets.UTF_8)
        return "Content-Length: ${sanitized.size}\r\n\r\n".toByteArray(Charsets.US_ASCII) + sanitized
    }

    private fun readUntilDoubleNewline(): ByteArray? {
        val buf = mutableListOf<Byte>()
        var b3 = 0; var b2 = 0; var b1 = 0
        while (true) {
            val b = source.read()
            if (b < 0) return buf.takeIf { it.isNotEmpty() }?.toByteArray()
            buf.add(b.toByte())
            if (b3 == '\r'.code && b2 == '\n'.code && b1 == '\r'.code && b == '\n'.code) return buf.toByteArray()
            b3 = b2; b2 = b1; b1 = b
        }
    }

    private fun sanitizeMessage(json: String): String {
        return try {
            val root = JsonParser.parseString(json).asJsonObject
            // lsp4j dispatches this via MethodHandles.unreflectSpecial, which bypasses any
            // LanguageClient override and falls through to the interface default that throws
            // UnsupportedOperationException. Transform to a no-op notification so lsp4j
            // discards it silently rather than crashing.
            if (root.get("method")?.asString == "workspace/inlineValue/refresh") {
                return """{"jsonrpc":"2.0","method":"$/ignore"}"""
            }
            sanitizeIfHover(json)
        } catch (e: Exception) {
            json
        }
    }

    private fun sanitizeIfHover(json: String): String {
        return try {
            val root = JsonParser.parseString(json).asJsonObject
            val result = root.get("result")?.takeIf { it.isJsonObject }?.asJsonObject ?: return json
            val contents = result.get("contents") ?: return json

            when {
                contents.isJsonObject -> contents.asJsonObject
                    .takeIf { it.has("value") }
                    ?.addProperty("value", stripMarkdownTables(contents.asJsonObject.get("value").asString))
                contents.isJsonPrimitive ->
                    result.addProperty("contents", stripMarkdownTables(contents.asString))
            }
            root.toString()
        } catch (e: Exception) {
            json
        }
    }

    private fun stripMarkdownTables(markdown: String): String {
        val result = mutableListOf<String>()
        for (line in markdown.lines()) {
            val trimmed = line.trim()
            when {
                // Skip separator rows like |---|---|
                trimmed.matches(Regex("\\|[-:| ]+\\|")) -> {}
                // Convert table rows to plain text: | A | B | → A  B
                trimmed.startsWith("|") -> {
                    val cells = trimmed.trim('|').split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    if (cells.isNotEmpty()) result.add(cells.joinToString("  "))
                }
                else -> result.add(line)
            }
        }
        return result.joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}
