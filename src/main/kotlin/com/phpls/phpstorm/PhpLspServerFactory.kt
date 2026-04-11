package com.phpls.phpstorm

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider

class PhpLspServerFactory : LanguageServerFactory {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider =
        PhpLspServerConnectionProvider(project)

    override fun createLanguageClient(project: Project): LanguageClientImpl =
        LanguageClientImpl(project)
}
