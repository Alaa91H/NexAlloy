package io.github.nexalloy

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.name

class FilePathArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(
        parameters: ParameterDeclarations,
        context: ExtensionContext
    ): Stream<out Arguments> {
        println(Path(".").toAbsolutePath())
        val projectDir = Paths.get(".") //.toAbsolutePath().normalize()
        val testInputPath = projectDir.resolve("binaries")

        if (!Files.exists(testInputPath)) {
            // ParameterizedClass requires one invocation. The placeholder produces no
            // dynamic fingerprint tests because it does not identify a supported app.
            return Stream.of(Arguments.of(Path("__no_apk_fixture__")))
        }

        val fixtures = Files.walk(testInputPath).use { paths ->
            paths.filter { path ->
                Files.isRegularFile(path) && path.normalize().none { it.name.startsWith(".") }
            }.toList()
        }
        if (fixtures.isEmpty()) {
            return Stream.of(Arguments.of(Path("__no_apk_fixture__")))
        }
        return fixtures.stream().map { Arguments.of(it) }
    }
}