package biz.atomeo.l9.service;

import biz.atomeo.l9.IOAdapter;
import biz.atomeo.l9.L9Game;
import biz.atomeo.l9.L9GameStarter;
import biz.atomeo.l9.L9GameState;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySessionProvider implements SessionProvider {
    private final ConcurrentHashMap<Long, SessionDTO> sessions = new ConcurrentHashMap<>();

    @Autowired
    private L9GameFilesProvider gameFilesProvider;

    @Override
    public SessionDTO getSession(Long chatId) throws L9Exception {
        //TODO: in future need to save and restore states in some storage
        return sessions.computeIfAbsent(chatId,
                chatId1 -> SessionDTO.builder()
                    .chatId(chatId)
                    .gameState(new L9GameState())
                    .gameService(L9GameStarter.buildGame(L9Game.WORM_PC, new IOAdapter() {
                        @Override
                        public String getGamePath(L9Game game) {
                            return gameFilesProvider.getGamePath(game);
                        }

                        @Override
                        public String getPicPath(L9Game game) {
                            return gameFilesProvider.getPicturePath(game);
                        }

                        @Override
                        public byte[] loadFile(String fileName) {
                            try {
                                return gameFilesProvider.readGameFile(fileName);
                            } catch (L9Exception e) {
                                return null;
                            }
                        }
                    }))
                    .build()
        );
    }

    @Override
    public void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception {
        // no need yet
    }
}
