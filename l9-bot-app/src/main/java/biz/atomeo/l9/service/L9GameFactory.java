package biz.atomeo.l9.service;

import biz.atomeo.l9.api.IOAdapter;
import biz.atomeo.l9.bot.L9GameService;
import biz.atomeo.l9.bot.L9GameState;
import biz.atomeo.l9.constants.L9Game;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import biz.atomeo.l9.graphics.L9Picture;
import biz.atomeo.l9.utils.PicUtils;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class L9GameFactory {
    private final L9GameFilesProvider gameFilesProvider;

    private List<String> pictures = new ArrayList<>();

    public void startGame(SessionDTO session, L9Game game) throws L9Exception {
        try {
            L9GameService service = new L9GameService(game, new IOAdapter() {
                @Override
                public String getGamePath(L9Game game) {
                    return gameFilesProvider.getGamePath(game);
                }

                @Override
                public String getPicPath(L9Game game) {
                    return gameFilesProvider.getPicturePath(game);
                }

                @Override
                public boolean fileExist(String file) {
                    //TODO from library
                    return false;
                }

                @Override
                public byte[] fileLoadRelativeToArray(String file) {
                    //TODO
                    return new byte[0];
                }

                @Override
                public boolean isPictureCached(int pictureNumber) {
                    String picFileName = gameFilesProvider.getPicturesCacheFilename(game, pictureNumber);
                    pictures.add(picFileName);
                    return gameFilesProvider.isFileExists(picFileName);
                }

                @Override
                public void cachePicture(int pictureNumber, List<L9Picture> frames) {
                    log.debug("Picture ready for save, frames count: {}", frames.size());
                    StreamingGifWriter writer = new StreamingGifWriter(Duration.ofMillis(20), false, true);
                    String picFileName = gameFilesProvider.getPicturesCacheFilename(game, pictureNumber);
                    try (StreamingGifWriter.GifStream gif = writer.prepareStream(picFileName, BufferedImage.TYPE_INT_ARGB)) {
                        FileUtils.createParentDirectories(new File(picFileName));
                        int i = frames.size();
                        if (frames.size()>1) {
                            ImmutableImage img = null;
                            for (L9Picture pic : frames) {
                                img = PicUtils.l9PictureToImmutableImage(pic);
                                gif.writeFrame(img, Duration.ofMillis(--i == 0 ? 120000 : 20));
                            }
                            if (frames.size() > 1) {
                                gif.writeFrame(img, Duration.ofMillis(120000)); //try to solve problem with restart playing gifs in tg i
                            }
                        } else if (frames.size()==1) {
                            ImmutableImage img = PicUtils.l9PictureToImmutableImage(frames.get(0)).scale(3);
                            gif.writeFrame(img);
                        }
                    } catch (Exception e) {
                        //
                    }
                }

                @Override
                public Object popPictures() {
                    List<String> pics = new ArrayList<>(pictures);
                    pictures.clear();
                    return pics;
                }

                @Override
                public byte[] loadFile(String fileName) {
                    try {
                        return gameFilesProvider.readGameFile(fileName);
                    } catch (L9Exception e) {
                        return null;
                    }
                }
            });
            L9GameState gameState = new L9GameState();

            session.setGameState(gameState);
            session.setGameService(service);
        } catch (Exception e) {
            session.setGameService(null);
            session.setChatState(null);
            throw new L9Exception("Error creating game", e);
        }
    }
}
