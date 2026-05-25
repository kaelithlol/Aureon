package com.kaelith.aureon.api.handlers

import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Module
object Ether {
    private val root = FabricLoader.getInstance().configDir.resolve(AureonCore.NAMESPACE)
    private val assetFolder = root.resolve("assets")
    private val versionFile = root.resolve("version.txt")

    init { sync() }

    fun sync(scope: CoroutineScope = AureonCore.scope) = scope.launch {
        if (!Files.exists(assetFolder)) Files.createDirectories(assetFolder)

        val remoteHash = Quasar.fetchString("${AureonCore.ETHER}/version.txt").getOrNull()?.trim() ?: return@launch
        val localHash = if (Files.exists(versionFile)) Files.readString(versionFile).trim() else ""

        if (remoteHash != localHash) {
            AureonCore.LOGGER.info("[Ether] Updating assets")

            val tempZip = Files.createTempFile("Aureon_assets", ".zip")
            Quasar.downloadFile("${AureonCore.ETHER}/assets.zip", tempZip).onSuccess {
                try {
                    extractZip(tempZip, assetFolder)
                    Files.writeString(versionFile, remoteHash)
                    AureonCore.LOGGER.info("[Ether] Asset sync complete.")
                } catch (e: Exception) {
                    AureonCore.LOGGER.error("[Ether] Failed to extract assets!", e)
                }
            }.onFailure { AureonCore.LOGGER.error("[Ether] Failed to download assets!", it) }
            Files.deleteIfExists(tempZip)
        }
    }

    private fun extractZip(zipPath: Path, destDir: Path) {
        ZipInputStream(Files.newInputStream(zipPath)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val newPath = destDir.resolve(entry.name).normalize()
                if (!newPath.startsWith(destDir)) throw kotlinx.io.IOException("Bad zip entry: ${entry.name}")

                if (entry.isDirectory) {
                    Files.createDirectories(newPath)
                } else {
                    Files.createDirectories(newPath.parent)
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING)
                }
                entry = zis.nextEntry
            }
        }
    }
}