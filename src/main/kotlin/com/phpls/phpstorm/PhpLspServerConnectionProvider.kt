package com.phpls.phpstorm

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class PhpLspServerConnectionProvider(project: Project) : ProcessStreamConnectionProvider() {
    init {
        val binary = extractBundledBinary() ?: File("php-lsp")
        commands = listOf(binary.absolutePath, "--stdio")
        workingDirectory = project.basePath
    }

    override fun start() {
        val binary = File(commands!!.first())
        if (!binary.exists() && !binary.isAbsolute) {
            notify(
                "php-lsp not found",
                "The <b>php-lsp</b> binary could not be found. " +
                    "Please report this issue at <a href=\"https://github.com/jorgsowa/php-lsp-phpstorm-plugin\">github.com/jorgsowa/php-lsp-phpstorm-plugin</a>.",
            )
            return
        }
        super.start()
    }

    private fun extractBundledBinary(): File? {
        val platform = detectPlatform() ?: return null
        val binaryName = if (SystemInfo.isWindows) "php-lsp-$platform.exe" else "php-lsp-$platform"
        val resource = javaClass.getResourceAsStream("/binaries/$binaryName") ?: return null

        val cacheDir = File(PathManager.getSystemPath(), "php-lsp-server").also { it.mkdirs() }
        val binary = File(cacheDir, binaryName)

        resource.use { Files.copy(it, binary.toPath(), StandardCopyOption.REPLACE_EXISTING) }
        binary.setExecutable(true)
        return binary
    }

    private fun detectPlatform(): String? = when {
        SystemInfo.isMac && SystemInfo.OS_ARCH == "aarch64" -> "aarch64-apple-darwin"
        SystemInfo.isMac -> "x86_64-apple-darwin"
        SystemInfo.isWindows -> "x86_64-pc-windows-msvc"
        SystemInfo.isLinux -> "x86_64-unknown-linux-gnu"
        else -> null
    }

    private fun notify(title: String, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("PHP LSP")
            .createNotification(title, content, NotificationType.ERROR)
            .notify(null)
    }
}
