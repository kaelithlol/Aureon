package com.kaelith.aureon.api.handlers

import com.google.gson.annotations.SerializedName
import com.kaelith.aureon.AureonCore
import com.kaelith.aureon.annotations.Module
import com.kaelith.aureon.events.EventBus
import com.kaelith.aureon.events.core.GameEvent
import com.kaelith.aureon.events.core.TickEvent
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Util
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.Comparator
import java.util.concurrent.atomic.AtomicBoolean
import java.util.jar.JarFile

@Module
object AutoUpdater {
    private const val CHECK_INTERVAL_MS = 15L * 60L * 1000L
    private const val PENDING_UPDATE_SUFFIX = ".aureon-update"
    private const val UPDATE_REPO = "kaelithlol/aureon"
    private const val EXPECTED_MOD_ID = "aureon"
    private const val EXPECTED_ASSET_PREFIX = "aureon-"
    private const val INSTALLER_MAIN_CLASS = "com.kaelith.aureon.api.handlers.UpdateInstaller"
    private val INSTALLER_CLASS_RESOURCE = "/" + INSTALLER_MAIN_CLASS.replace('.', '/') + ".class"

    private val checking = AtomicBoolean(false)
    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    @Volatile private var lastCheckStartedMs = 0L
    @Volatile private var pendingUpdate: PendingUpdate? = null
    var enabled by Capsule("autoUpdaterEnabled", true)
    @Volatile var statusLine = "Ready."
        private set

    init {
        EventBus.on<TickEvent.Client> {
            if (!enabled || checking.get()) return@on
            val now = Util.getMillis()
            if (lastCheckStartedMs == 0L || now - lastCheckStartedMs >= CHECK_INTERVAL_MS) {
                checkForUpdatesAsync(manual = false)
            }
        }

        EventBus.on<GameEvent.Stop> {
            if (enabled) trySchedulePendingInstall()
        }
    }

    fun checkForUpdatesAsync(manual: Boolean) {
        if (!enabled && !manual) {
            statusLine = "Auto updater reminders are disabled."
            return
        }

        if (!checking.compareAndSet(false, true)) {
            setStatus("Already checking GitHub for updates...")
            return
        }

        lastCheckStartedMs = Util.getMillis()
        setStatus("Checking GitHub releases...")

        Thread.startVirtualThread {
            try {
                performCheck(approvedUpdate = null)
            } catch (e: Exception) {
                setStatus("Update check failed.")
                printError("Failed to check for updates", e)
            } finally {
                checking.set(false)
            }
        }
    }

    fun downloadApprovedUpdateAsync() {
        if (!enabled) {
            setStatus("Auto updater reminders are disabled. Use /aureon updates on to enable them again.")
            return
        }

        val approvedUpdate = pendingUpdate
        if (approvedUpdate == null) {
            setStatus("No reviewed update is waiting for approval. Checking GitHub releases first.")
            checkForUpdatesAsync(manual = true)
            return
        }

        if (!checking.compareAndSet(false, true)) {
            setStatus("Already checking GitHub for updates...")
            return
        }

        lastCheckStartedMs = Util.getMillis()
        setStatus("Downloading approved update ${approvedUpdate.tagName}...")

        Thread.startVirtualThread {
            try {
                performCheck(approvedUpdate)
            } catch (e: Exception) {
                setStatus("Update download failed.")
                printError("Failed to download approved update", e)
            } finally {
                checking.set(false)
            }
        }
    }

    fun setEnabledState(enabled: Boolean) {
        this.enabled = enabled
        if (enabled) {
            setStatus("Auto updater reminders enabled.")
            checkForUpdatesAsync(manual = true)
        } else {
            pendingUpdate = null
            setStatus("Auto updater reminders disabled. Use /aureon updates on to enable them again.")
        }
    }

    fun clearPendingUpdate() {
        resolveCurrentJarPath()
            ?.let(::pendingPathFor)
            ?.let {
                try {
                    Files.deleteIfExists(it)
                } catch (e: IOException) {
                    printError("Failed to clear pending update $it", e)
                }
            }
    }

    private fun performCheck(approvedUpdate: PendingUpdate?) {
        val request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/$UPDATE_REPO/releases/latest"))
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "Aureon AutoUpdater")
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build()

        val releaseResponse = http.send(request, HttpResponse.BodyHandlers.ofString())
        if (releaseResponse.statusCode() != 200) {
            setStatus("GitHub responded with HTTP ${releaseResponse.statusCode()}.")
            return
        }

        val release = Quasar.gson.fromJson(releaseResponse.body(), GithubRelease::class.java)
        val asset = release?.assets
            ?.filterNotNull()
            ?.firstOrNull {
                it.browserDownloadUrl != null &&
                    it.name != null &&
                    it.name!!.endsWith(".jar") &&
                    !it.name!!.contains("-sources") &&
                    it.name!!.startsWith(EXPECTED_ASSET_PREFIX)
            }

        if (asset?.browserDownloadUrl == null || asset.name == null) {
            setStatus("Release exists, but there is no Aureon jar asset to download.")
            return
        }
        val assetName = asset.name!!
        val assetDownloadUrl = asset.browserDownloadUrl!!

        val currentVersion = FabricLoader.getInstance().getModContainer(EXPECTED_MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
        val latestVersion = normalizeVersion(release.tagName)

        val currentJar = resolveCurrentJarPath()
        val destination = if (currentJar != null) {
            pendingPathFor(currentJar)
        } else {
            val updateDir = FabricLoader.getInstance().configDir.resolve(AureonCore.NAMESPACE).resolve("updates")
            Files.createDirectories(updateDir)
            updateDir.resolve(assetName)
        }

        if (latestVersion == normalizeVersion(currentVersion)) {
            currentJar?.let(::pendingPathFor)?.let { Files.deleteIfExists(it) }
            pendingUpdate = null
            setStatus("You're up to date on $currentVersion.")
            return
        }

        if (Files.exists(destination) && (asset.size <= 0 || Files.size(destination) == asset.size)) {
            pendingUpdate = null
            setStatus(if (currentJar != null) {
                "Update ${release.tagName} is ready. Restart the game to apply it."
            } else {
                "Update ${release.tagName} is already downloaded to config/aureon/updates."
            })
            return
        }

        val updateToDownload = PendingUpdate(
            tagName = release.tagName ?: latestVersion,
            assetName = assetName,
            downloadUrl = assetDownloadUrl,
            size = asset.size
        )
        if (approvedUpdate != updateToDownload) {
            pendingUpdate = updateToDownload
            requestDownloadConsent(updateToDownload, currentJar != null)
            return
        }

        pendingUpdate = null
        if (currentJar != null) cleanupSiblingPending(currentJar, destination)
        else cleanupFallbackDownloads(destination.parent, destination)

        val downloadRequest = HttpRequest.newBuilder(URI.create(assetDownloadUrl))
            .header("User-Agent", "Aureon AutoUpdater")
            .timeout(Duration.ofSeconds(60))
            .GET()
            .build()
        val downloadResponse = http.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream())
        if (downloadResponse.statusCode() != 200) {
            setStatus("Download failed with HTTP ${downloadResponse.statusCode()}.")
            return
        }

        val tempFile = destination.resolveSibling("${destination.fileName}.part")
        downloadResponse.body().use { input -> Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING) }
        if (!verifyDownloadedJar(tempFile)) {
            Files.deleteIfExists(tempFile)
            setStatus("Downloaded file failed verification.")
            return
        }

        try {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: IOException) {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING)
        }

        setStatus(if (currentJar != null) {
            "Downloaded ${release.tagName}. Restart the game to install it automatically."
        } else {
            "Downloaded ${release.tagName} to config/aureon/updates."
        })
        printLine("Downloaded $assetName to ${destination.toAbsolutePath()}")
    }

    private fun resolveCurrentJarPath(): Path? = runCatching {
        val path = Path.of(AureonCore::class.java.protectionDomain.codeSource.location.toURI()).toAbsolutePath()
        path.takeIf { Files.isRegularFile(it) && it.fileName.toString().endsWith(".jar") }
    }.onFailure {
        printError("Unable to resolve current jar path", it)
    }.getOrNull()

    private fun pendingPathFor(currentJar: Path): Path =
        currentJar.resolveSibling(currentJar.fileName.toString() + PENDING_UPDATE_SUFFIX)

    private fun trySchedulePendingInstall() {
        val currentJar = resolveCurrentJarPath() ?: return
        val stagedFile = pendingPathFor(currentJar)
        if (!Files.exists(stagedFile)) return

        try {
            launchInstaller(currentJar, stagedFile)
            setStatus("Update is queued and will be installed as the game closes.")
        } catch (e: IOException) {
            setStatus("Update downloaded, but failed to schedule install.")
            printError("Failed to launch installer", e)
        }
    }

    private fun setStatus(message: String) {
        statusLine = message
        printLine(message)
    }

    private fun requestDownloadConsent(update: PendingUpdate, stagesForInstall: Boolean) {
        val message = "Update ${update.tagName} available: ${update.assetName} (${formatBytes(update.size)}). Waiting for user consent."
        setStatus(message)
        runCatching {
            Signal.fakeMessage("${AureonCore.PREFIX} §bAureon ${update.tagName} is available.")
            Signal.fakeMessage("${AureonCore.PREFIX} §7Download §f${update.assetName} §7(${formatBytes(update.size)}) from GitHub${if (stagesForInstall) " and stage it for install on restart" else ""}?")
            Signal.fakeMessage(
                Component.literal("${AureonCore.PREFIX} ")
                    .append(
                        Component.literal("§a[Download]")
                            .withStyle { it.withClickEvent(ClickEvent.RunCommand("/aureon updates download")) }
                    )
                    .append(Component.literal(" §7or run §f/aureon updates off §7to stop these reminders."))
            )
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "unknown size"
        val mib = bytes / (1024.0 * 1024.0)
        return if (mib >= 1.0) "%.1f MiB".format(mib) else "$bytes bytes"
    }

    private fun printLine(message: String) {
        println("[Aureon AutoUpdater] $message")
    }

    private fun printError(message: String, throwable: Throwable) {
        System.err.println("[Aureon AutoUpdater] $message")
        throwable.printStackTrace(System.err)
    }

    private fun launchInstaller(currentJar: Path, stagedFile: Path) {
        val javaExe = Path.of(
            System.getProperty("java.home"),
            "bin",
            if (System.getProperty("os.name", "").lowercase().contains("win")) "javaw.exe" else "java"
        ).toString()

        val installerRoot = Files.createTempDirectory("aureon-updater-").toAbsolutePath()
        val installerClass = installerRoot.resolve(INSTALLER_MAIN_CLASS.replace('.', '/') + ".class")
        Files.createDirectories(installerClass.parent)
        AutoUpdater::class.java.getResourceAsStream(INSTALLER_CLASS_RESOURCE).use { input ->
            if (input == null) throw IOException("Unable to locate $INSTALLER_CLASS_RESOURCE in mod jar")
            Files.copy(input, installerClass, StandardCopyOption.REPLACE_EXISTING)
        }

        val logFile = currentJar.resolveSibling("aureon-updater.log")
        ProcessBuilder(
            javaExe,
            "-cp",
            installerRoot.toString(),
            INSTALLER_MAIN_CLASS,
            ProcessHandle.current().pid().toString(),
            stagedFile.toAbsolutePath().toString(),
            currentJar.toAbsolutePath().toString(),
            installerRoot.toString()
        )
            .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()))
            .redirectError(ProcessBuilder.Redirect.appendTo(logFile.toFile()))
            .start()
    }

    private fun verifyDownloadedJar(jarPath: Path): Boolean = try {
        JarFile(jarPath.toFile()).use { jar ->
            val entry = jar.getEntry("fabric.mod.json") ?: return false
            jar.getInputStream(entry).use(InputStream::readAllBytes).decodeToString().let {
                it.contains("\"id\": \"$EXPECTED_MOD_ID\"") || it.contains("\"id\":\"$EXPECTED_MOD_ID\"")
            }
        }
    } catch (e: Exception) {
        printError("Downloaded jar failed verification", e)
        false
    }

    private fun cleanupSiblingPending(currentJar: Path, keepFile: Path) {
        val siblingDir = currentJar.parent
        val currentJarName = currentJar.fileName.toString()
        try {
            Files.list(siblingDir).use { files ->
                files.filter { it != keepFile }
                    .filter { it.fileName.toString().startsWith(currentJarName) && it.fileName.toString().contains(PENDING_UPDATE_SUFFIX) }
                    .forEach { Files.deleteIfExists(it) }
            }
        } catch (e: IOException) {
            printError("Failed to clean pending updates near $currentJar", e)
        }
    }

    private fun cleanupFallbackDownloads(updateDir: Path, keepFile: Path) {
        try {
            Files.list(updateDir).use { files ->
                files.filter { it != keepFile }
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach { Files.deleteIfExists(it) }
            }
        } catch (e: IOException) {
            printError("Failed to clean update directory $updateDir", e)
        }
    }

    private fun normalizeVersion(version: String?): String =
        version?.trim()?.removePrefix("v")?.removePrefix("V") ?: ""

    private class GithubRelease {
        @SerializedName("tag_name")
        var tagName: String? = null
        var assets: Array<GithubAsset?>? = null
    }

    private class GithubAsset {
        var name: String? = null
        var size: Long = 0
        @SerializedName("browser_download_url")
        var browserDownloadUrl: String? = null
    }

    private data class PendingUpdate(
        val tagName: String,
        val assetName: String,
        val downloadUrl: String,
        val size: Long
    )
}
