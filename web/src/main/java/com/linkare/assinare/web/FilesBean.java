package com.linkare.assinare.web;

import java.io.File;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author bnazare
 */
public class FilesBean {

    public List<FileMetadata> getOriginalFiles() {
        List<FileMetadata> files = getDirFiles(Config.getOriginalFilesLocation());
        files.sort(
                (fm1, fm2) -> fm1.getName().compareTo(fm2.getName())
        );

        return files;
    }

    public List<FileMetadata> getSignedFiles(String sessionId) {
        List<FileMetadata> files = getDirFiles(Config.getSignedFilesLocation() + "/" + sessionId);
        files.sort(
                (fm1, fm2) -> fm2.getLastModified().compareTo(fm1.getLastModified())
        );

        return files;
    }

    public List<FileMetadata> getStoredFiles() {
        return getDirFiles(Config.getStoreFilesLocation());
    }

    public List<FileMetadata> getDirFiles(String dirPath) {
        File docsDir = new File(dirPath);
        List<FileMetadata> docsMetadata = new LinkedList<>();
        if (docsDir.exists()) {

            for (File doc : docsDir.listFiles(File::isFile)) {
                docsMetadata.add(new FileMetadata(doc));
            }
        }
        return docsMetadata;
    }

    public class FileMetadata {

        private static final int MAX_FILENAME_LENGTH = 26;
        private static final int SHORT_FILENAME_LENGTH = 22;

        private final String name;
        private final String shortName;
        private final Long lengthBytes;
        private final Instant lastModified;

        public FileMetadata(File f) {
            if (f.getName().length() > MAX_FILENAME_LENGTH) {
                this.shortName = f.getName().substring(0, SHORT_FILENAME_LENGTH) + "…";
            } else {
                this.shortName = null;
            }
            this.name = f.getName();
            this.lengthBytes = f.length();
            this.lastModified = Instant.ofEpochMilli(f.lastModified());
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public Long getLengthBytes() {
            return lengthBytes;
        }

        public Date getLastModified() {
            return Date.from(lastModified);
        }

    }
}
