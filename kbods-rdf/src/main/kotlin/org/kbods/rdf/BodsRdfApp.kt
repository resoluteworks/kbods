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
import org.kbods.rdf.plugins.BodsConvertPlugin
import org.kbods.read.BodsDownload
import org.slf4j.LoggerFactory
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
    val output by option("--output").required()
    val excludeVocabulary by option("--exclude-vocabulary").flag()
    val plugins: List<String> by option("--plugin").multiple(required = false)

    val config: BodsRdfConfig
        get() {
            val config = BodsRdfConfig()
            plugins.forEach { pluginName ->
                log.info("Adding plugin $pluginName")
                config.addPlugin(BodsConvertPlugin.getPlugin(pluginName))
            }
            return config
        }

    companion object {
        private val log = LoggerFactory.getLogger(CommonConvertOptions::class.java)
    }
}


class Convert : CliktCommand() {
    private val commonOptions by CommonConvertOptions()
    val input by option("--input").required()

    override fun run() {
        File(input)
            .convert(File(commonOptions.output), commonOptions.config, !commonOptions.excludeVocabulary)
    }
}

class ConvertLatest : CliktCommand() {
    private val commonOptions by CommonConvertOptions()
    override fun run() {
        BodsDownload.latest()
            .convert(File(commonOptions.output), commonOptions.config, !commonOptions.excludeVocabulary)
    }
}

