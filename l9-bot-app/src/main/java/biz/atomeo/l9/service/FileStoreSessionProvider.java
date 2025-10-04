package biz.atomeo.l9.service;

import biz.atomeo.l9.bot.L9GameService;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStoreSessionProvider implements SessionProvider {
    private final L9GameFilesProvider l9GameFilesProvider;
    private final L9GameFactory gameFactory;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Value("${sessionsDir:sessions/}")
    private String sessionsDirectory;

    @Override
    public SessionDTO getSession(Long chatId) throws L9Exception {
        SessionDTO session = null;
        try {
            String json = l9GameFilesProvider.readSessionFromFile(chatId);
            if (json==null) return null;
            session = OBJECT_MAPPER.readValue(json, SessionDTO.class);
            l9GameFilesProvider.writeSessionToFile(chatId, json);
        } catch (JsonProcessingException e) {
            log.error("Error session deserialization",e);
            return null;
        } catch (L9Exception e) {
            log.error("Error reading session",e);
            throw e;
        }

        if (ChatState.PLAYING_GAME.equals(session.getChatState())) {
            gameFactory.startGame(session, session.getGameState().getL9game());
            L9GameService gs = session.getGameService();
            if (!gs.readAutoSaveFile(l9GameFilesProvider.getSaveFilename(chatId, gs.getL9game()))) {
                throw new L9Exception("Unable to read save game file.");
            }
        }

        return session;
    }

    @Override
    public void updateSession(Long chatId, SessionDTO session) throws L9Exception {
        if (session!=null) {
            //save bot session
            try {
                String json = OBJECT_MAPPER.writeValueAsString(session);
                l9GameFilesProvider.writeSessionToFile(chatId, json);
            } catch (JsonProcessingException e) {
                log.error("Error session serialization",e);
            }
            //if game is playing - need to save game state file
            if (ChatState.PLAYING_GAME.equals(session.getChatState())
                    && session.getGameService()!=null) {
                L9GameService gs = session.getGameService();
                if (!gs.writeAutoSaveFile(l9GameFilesProvider.getSaveFilename(chatId, gs.getL9game()))) {
                    throw new L9Exception("Unable to save game file.");
                }
            }
        }
    }
}
