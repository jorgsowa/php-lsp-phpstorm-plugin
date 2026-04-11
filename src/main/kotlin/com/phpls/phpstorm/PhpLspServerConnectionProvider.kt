package com.phpls.phpstorm

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider

class PhpLspServerConnectionProvider(project: Project) : ProcessStreamConnectionProvider() {
    init {
        // Assumes `php-lsp` is on PATH; adjust or add a settings page to make this configurable
        commands = listOf("php-lsp", "--stdio")
        workingDirectory = project.basePath
    }
}
