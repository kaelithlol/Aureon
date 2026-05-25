package com.kaelith.aureon.api.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class UpdateInstaller {
    private UpdateInstaller() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            return;
        }

        long pid = Long.parseLong(args[0]);
        Path stagedFile = Path.of(args[1]);
        Path targetJar = Path.of(args[2]);
        Path installerRoot = Path.of(args[3]);

        ProcessHandle.of(pid).ifPresent(handle -> {
            while (handle.isAlive()) {
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        for (int attempt = 0; attempt < 40; attempt++) {
            try {
                Files.move(stagedFile, targetJar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                break;
            } catch (IOException e) {
                if (attempt == 39) {
                    Files.move(stagedFile, targetJar, StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
                Thread.sleep(250L);
            }
        }

        deleteRecursively(installerRoot);
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
