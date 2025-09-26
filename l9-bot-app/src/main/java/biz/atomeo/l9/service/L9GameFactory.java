package biz.atomeo.l9.service;

import biz.atomeo.l9.*;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class L9GameFactory {
    private final L9GameFilesProvider gameFilesProvider;

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
