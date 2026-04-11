package com.phpls.phpstorm

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import java.util.concurrent.CompletableFuture

class PhpLspServerFactory : LanguageServerFactory {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider =
        PhpLspServerConnectionProvider(project)

    override fun createLanguageClient(project: Project): LanguageClientImpl =
        PhpLspLanguageClient(project)
}

private class PhpLspLanguageClient(project: Project) : LanguageClientImpl(project) {
    override fun refreshDiagnostics(): CompletableFuture<Void> =
        CompletableFuture.completedFuture(null)

    override fun refreshInlineValues(): CompletableFuture<Void> =
        CompletableFuture.completedFuture(null)
}
