package ir.msob.manak.memory.util;

import ir.msob.manak.domain.model.util.chunk.Chunker;


public final class FileTypeDetector {


    private FileTypeDetector() {
    }


    public static Chunker.FileType detect(String path) {
        if (path == null || path.isBlank()) return Chunker.FileType.GENERIC;


        String fileName = path.substring(Math.max(0, path.lastIndexOf('/') + 1));
        int dot = fileName.lastIndexOf('.');
        String ext = dot > -1 && dot < fileName.length() - 1 ? fileName.substring(dot + 1).toLowerCase() : "";


        return switch (ext) {
            case "md" -> Chunker.FileType.MARKDOWN;
            case "xml" -> Chunker.FileType.XML;
            case "pom" -> Chunker.FileType.POM;
            case "java" -> Chunker.FileType.JAVA;
            case "yml", "yaml" -> Chunker.FileType.YAML;
            case "properties" -> Chunker.FileType.PROPERTIES;
            case "ts", "tsx" -> Chunker.FileType.TYPESCRIPT;
            case "json" -> Chunker.FileType.JSON;
            default -> Chunker.FileType.GENERIC;
        };
    }
}