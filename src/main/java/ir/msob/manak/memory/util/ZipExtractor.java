package ir.msob.manak.memory.util;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public final class ZipExtractor {
    private static final Logger log = LoggerFactory.getLogger(ZipExtractor.class);


    private ZipExtractor() {
    }


    public static Map<String, byte[]> extractIndexableFiles(byte[] zipBytes, Set<String> indexableExts) {
        Map<String, byte[]> fileMap = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) continue;
                String name = ze.getName();
                String base = name.substring(Math.max(0, name.lastIndexOf('/') + 1));
                if (base.startsWith(".")) continue; // hidden
                String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
                if (!indexableExts.contains(ext.toLowerCase())) {
                    log.debug("Skipping non-indexable file: {}", name);
                    continue;
                }
                byte[] data = zis.readAllBytes();
                fileMap.put(name, data);
            }
            log.debug("Zip extracted, indexable files={}", fileMap.size());
        } catch (IOException e) {
            log.error("Failed to unpack zip: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unpack repository zip", e);
        }
        return fileMap;
    }
}