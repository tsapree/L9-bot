package biz.atomeo.l9.service;

import biz.atomeo.l9.L9Game;
import biz.atomeo.l9.config.L9AppProperties;
import biz.atomeo.l9.error.L9Exception;
import biz.atomeo.l9.utils.FileIOUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Service
@Slf4j
public class L9GameFilesProvider {

    @Value("${downloadsDir}")
    private String downloadsDirectory;

    @Value("${gamesDir}")
    private String gamesDirectory;

    @Value("${l9source}")
    private String l9source;

    @Autowired
    private L9AppProperties l9AppProperties;

    @PostConstruct
    public void afterStart() throws L9Exception {
        log.info("GET CONFIG: {}", l9AppProperties.getGames().get("EMERALD_ISLE").getArchive());
        l9AppProperties.getGames().forEach((key, value) -> {
            try {
                log.info("prepare {}", key);
                checkAndPrepareGameFile(value.getPath(), value.getArchive(), value.getPath());
            } catch (L9Exception e) {
                //
            }
        });
    }

    public String getGamePath(L9Game l9Game) {
        return l9AppProperties.getGames().get(l9Game.name()).getPath();
    }

    public String getPicturePath(L9Game l9Game) {
        return l9AppProperties.getGames().get(l9Game.name()).getPic();
    }

    public byte[] readGameFile(String filename) throws L9Exception {
        try {
            return FileUtils.readFileToByteArray(new File(gamesDirectory + filename));
        } catch (IOException e) {
            throw new L9Exception("Read file error", e);
        }
    }

    public void checkAndPrepareGameFile(String filename, String archiveName, String unpackFolder) throws L9Exception {
        log.info("Check is file {} exists", filename);
        if (checkGameFileExist(filename)) return;
        log.info("Check is archive {} exists - and download if not", archiveName);
        downloadL9Archive(archiveName);
        log.info("Unzip archive {} to folder {}", archiveName, unpackFolder);
        unzipL9Archive(archiveName, unpackFolder);
    }

    public boolean checkGameFileExist(String filename) {
        return FileIOUtils.isFileExists(gamesDirectory+filename);
    }

    public void downloadL9Archive(String name) throws L9Exception {
        if (FileIOUtils.isFileExists(downloadsDirectory+name)) return;
        downloadFileToPath(l9source+name, downloadsDirectory+name);
    }

    public void unzipL9Archive(String name, String folder) throws L9Exception{
        try {
            FileIOUtils.unzip(downloadsDirectory+name, gamesDirectory+folder);
        } catch (IOException e) {
            throw new L9Exception("unzip error", e);
        }
    }

    public void downloadFileToPath(String url, String path) throws L9Exception{
        try {
            log.info(new File(path).getAbsolutePath());
            FileUtils.copyURLToFile(new URL(url), new File(path));
        } catch (IOException e) {
            throw new L9Exception("file download error: ", e);
        }
    }

}
