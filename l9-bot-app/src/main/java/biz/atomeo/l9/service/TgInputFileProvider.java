package biz.atomeo.l9.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TgInputFileProvider {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @Autowired
    ObjectMapper objectMapper;

    @Value("${picturesDir:tmp/cache/}")
    private String picturesCacheDirectory;

    @PostConstruct
    public void init() {
        readTgFileIdIndex();
    }

    @PreDestroy
    public void destroy() {
        writeTgFileIdIndex();
    }

    public boolean isFileCached(String filename) {
        return cache.containsKey(filename);
    }

    public InputFile getInputFile(String filename) {
        if (cache.containsKey(filename)) {
            return new InputFile(cache.get(filename));
        }
        return new InputFile(new File(filename));
    }

    public void cachePhotoFileId(InputFile inputFile, String filename, Message message) {
        if (inputFile.isNew()) {
            List<PhotoSize> photos = message.getPhoto();
            if (photos!=null && !photos.isEmpty()) {
                PhotoSize ps = photos.stream().reduce(new PhotoSize(null, null, 0, 0, 0, null),
                                (p1, p2) -> p1.getWidth() > p2.getWidth() ? p1 : p2);
                if (ps.getFileId()!=null) {
                    cache.put(filename, ps.getFileId());
                }
            }
        }
    }

    private void readTgFileIdIndex() {
        try {
            String json = FileUtils.readFileToString(new File(String.format("%sfile_ids.json", picturesCacheDirectory)),
                    "UTF-8");
            cache.putAll(objectMapper.readValue(json, HashMap.class));
        } catch (Exception e) {
            log.debug("Error reading file_ids.json, it's ok. We'll start with new one");
        }
    }

    public void writeTgFileIdIndex() {
        try {
            String json = objectMapper.writeValueAsString(cache);
            FileUtils.writeStringToFile(new File(String.format("%sfile_ids.json", picturesCacheDirectory)),
                    json, "UTF-8");
        } catch (Exception e) {
            log.error("Error writing file_ids.json", e);
        }
    }
}
