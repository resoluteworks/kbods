package org.kbods.rdf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.kbods.rdf.convert.BodsConverter
import org.kbods.read.BodsDownload
import java.io.File

object BodsRdfApp {
    @JvmStatic
    fun main(args: Array<String>) {
        BodsRdfAppCommand()
            .subcommands(Convert(), ConvertLatest())
            .main(args)
    }
}

class BodsRdfAppCommand : NoOpCliktCommand()

class CommonConvertOptions : OptionGroup() {
    val outputs: List<String> by option("--output").multiple(required = true)
    val plugins: List<String> by option("--plugin").multiple(required = false)
    val relationshipsOnly: Boolean by option("--relationships-only").flag(default = false)
    val config: BodsRdfConfig get() = BodsRdfConfig(relationshipsOnly = relationshipsOnly).withPlugins(plugins)
}

class Convert : CliktCommand() {
    private val commonOptions by CommonConvertOptions()
    val input by option("--input").required()

    override fun run() {
        BodsConverter(commonOptions.config, commonOptions.outputs.map { File(it) })
            .convert(File(input))
    }
}

class ConvertLatest : CliktCommand() {
    private val commonOptions by CommonConvertOptions()
    override fun run() {
        BodsConverter(commonOptions.config, commonOptions.outputs.map { File(it) })
            .convert(BodsDownload.latest())
    }
}

