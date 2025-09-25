package biz.atomeo.l9.service;

import biz.atomeo.l9.L9Game;
import biz.atomeo.l9.L9GameStarter;
import biz.atomeo.l9.L9GameState;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySessionProvider implements SessionProvider {
    private ConcurrentHashMap<Long, SessionDTO> sessions = new ConcurrentHashMap<>();

    @Override
    public SessionDTO getSession(Long chatId) throws L9Exception {
        //TODO: in future need to save and restore states in some storage
        return sessions.computeIfAbsent(chatId,
                chatId1 -> SessionDTO.builder()
                    .chatId(chatId)
                    .gameState(new L9GameState())
                    .gameService(L9GameStarter.buildGame(L9Game.EMERALD_ISLE))
                    .build()
        );
    }

    @Override
    public void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception {
        // no need yet
    }
}
