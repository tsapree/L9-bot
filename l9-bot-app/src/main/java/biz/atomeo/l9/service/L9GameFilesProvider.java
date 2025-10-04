package biz.atomeo.l9.service;

import biz.atomeo.l9.constants.L9Game;
import biz.atomeo.l9.config.L9AppProperties;
import biz.atomeo.l9.dto.GameInfoDTO;
import biz.atomeo.l9.error.L9Exception;
import biz.atomeo.l9.utils.FileIOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

@Service
@Slf4j
public class L9GameFilesProvider {

    @Value("${downloadsDir:tmp/downloads/}")
    private String downloadsDirectory;

    @Value("${gamesDir:tmp/games/}")
    private String gamesDirectory;

    @Value("${picturesDir:tmp/cache/}")
    private String picturesCacheDirectory;

    @Value("${sessionsDir:sessions/}")
    private String sessionsDirectory;

    @Value("${l9source}")
    private String l9source;

    @Autowired
    private L9AppProperties l9AppProperties;

    public String getGamePath(L9Game l9Game) {
        GameInfoDTO gi = l9AppProperties.getGames().get(l9Game.name());
        try {
            //check that game exist in cache, if not - download and unzip it
            checkAndPrepareGameFile(gi.getPath(), gi.getArchive(), gi.getFolder());
        } catch (L9Exception e) {
            //
        }
        return gi.getPath();
    }

    public String getPicturePath(L9Game l9Game) {
        return l9AppProperties
                .getGames()
                .get(l9Game.name())
                .getPic();
    }

    public String getPicturesCacheFilename(L9Game l9Game, int picNumber) {
        String pf = picturesCacheDirectory+l9Game.getId()+"_pic"+picNumber+".gif";
        log.debug("pic cache path {}", pf);
        return pf;
    }

    public boolean isFileExists(String path) {
        boolean exist = FileIOUtils.isFileExists(path);
        log.debug("file {} {}", path, exist ? "exist" : "not found");
        return exist;
    }

    public byte[] readGameFile(String filename) throws L9Exception {
        try {
            return FileUtils.readFileToByteArray(new File(gamesDirectory + filename));
        } catch (IOException e) {
            throw new L9Exception("Read file error", e);
        }
    }

    public byte[] readSaveFile(String filename) throws L9Exception {
        try {
            return FileUtils.readFileToByteArray(new File(sessionsDirectory + filename));
        } catch (IOException e) {
            throw new L9Exception("Read file error", e);
        }
    }

    public void writeSaveFile(String filename, byte[] bytes) throws L9Exception {
        try {
            FileUtils.writeByteArrayToFile(new File(sessionsDirectory + filename), bytes);
        } catch (IOException e) {
            throw new L9Exception("Read file error", e);
        }
    }

    public void writeSessionToFile(Long chatId, String source) throws L9Exception {
        try {
            FileUtils.writeStringToFile(new File(String.format("%s%d.json", sessionsDirectory, chatId)), source, Charset.defaultCharset());
        } catch (IOException e) {
            throw new L9Exception("Write session file error", e);
        }
    }

    public String readSessionFromFile(Long chatId) throws L9Exception {
        try {
            String path = String.format("%s%d.json", sessionsDirectory, chatId);
            if (!FileIOUtils.isFileExists(path)) return null;
            return FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            throw new L9Exception("Read session file error", e);
        }
    }

    public String getSaveFilename(Long chatId, L9Game l9Game) {
        String pf = String.format("%d_%s.sav", chatId, l9Game.getId());
        log.debug("game save filename {}", pf);
        return pf;
    }

    public void checkAndPrepareGameFile(String filename, String archiveName, String unpackFolder) throws L9Exception {
        log.debug("Check is file {} exists", filename);
        if (checkGameFileExist(filename)) return;
        log.debug("Check is archive {} exists - and download if not", archiveName);
        downloadL9Archive(archiveName);
        log.debug("Unzip archive {} to folder {}", archiveName, unpackFolder);
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
            log.debug("Downloading: {}",new File(path).getAbsolutePath());
            FileUtils.copyURLToFile(new URL(url), new File(path));
        } catch (IOException e) {
            throw new L9Exception("file download error: ", e);
        }
    }

}
