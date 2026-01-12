package io.jettra.engine.files;

import io.jettra.engine.core.AbstractEngine;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FilesEngine extends AbstractEngine {

    public String getName() {
        return "Files";
    }

    public void init() {
        System.out.println("Files Engine initialized");
    }

    // Future implementation:
    // - Store files as BLOBs or reference local paths
    // - Metadata extraction
    // - Full-text search on file content
}
