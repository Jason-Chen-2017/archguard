package org.archguard.scanner.analyser

import chapi.domain.core.CodeDataStruct
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.archguard.scanner.core.sourcecode.LanguageSourceCodeAnalyser
import org.archguard.scanner.core.sourcecode.SourceCodeContext
import java.io.File

class GoAnalyser(override val context: SourceCodeContext) : LanguageSourceCodeAnalyser {
    private val client = context.client
    private val impl = chapi.ast.goast.GoAnalyser()

    override fun analyse(): List<CodeDataStruct> = runBlocking {
        getFilesByPath(context.path) {
            it.absolutePath.endsWith(".go")
        }
            .map { async { analysisByFile(it) } }.awaitAll()
            .flatten()
            .also { client.saveDataStructure(it) }
    }

    fun analyse(channel: Channel<CodeDataStruct>) = runBlocking {
        getFilesByPath(context.path) {
            it.absolutePath.endsWith(".go")
        }
            .map { async { analysisByFile(it) } }.awaitAll()
            .flatten()
            .forEach { channel.send(it) }
    }

    private fun analysisByFile(file: File): List<CodeDataStruct> {
        val codeContainer = impl.analysis(file.readContent(), file.name)

        return codeContainer.DataStructures.map {
            it.apply {
                it.Imports = codeContainer.Imports
                it.FilePath = file.absolutePath
            }
        }
    }
}
