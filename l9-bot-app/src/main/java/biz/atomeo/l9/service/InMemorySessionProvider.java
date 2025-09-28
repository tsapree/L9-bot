package biz.atomeo.l9.service;

import biz.atomeo.l9.constants.ChatState;
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
                    .chatState(ChatState.INIT)
                    .chatId(chatId)
                    .build()
        );
    }

    @Override
    public void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception {
        // no need yet
    }

    public int getSessionsCount() {
        return sessions.size();
    }
}
