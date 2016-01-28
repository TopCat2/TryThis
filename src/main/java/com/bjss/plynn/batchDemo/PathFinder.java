package com.bjss.plynn.batchDemo;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PathFinder {
    private String inDirectoryPath, inFileName;

    private PathFinder (String path, String name) {
        this.inFileName = name;
        this.inDirectoryPath = path;
    }

    public static PathFinder getPathFinder(String path, String name) {
        return new PathFinder(path, name);
    }

    public Path getInputFilePath() {
        return (FileSystems.getDefault().getPath(inDirectoryPath, inFileName));
    }

    public Path getWorkFilePath() {
        return (FileSystems.getDefault().getPath(inDirectoryPath, "inFlight", inFileName));
    }

    public Path getDoneFilePath() {
        return (FileSystems.getDefault().getPath(inDirectoryPath, "done", inFileName));
    }

    public void setFileName(String newName) {
        this.inFileName = newName;
    }

    public String getFileName() {
        return inFileName;
    }

    public String getDirPath() {
        return inDirectoryPath;
    }
}

