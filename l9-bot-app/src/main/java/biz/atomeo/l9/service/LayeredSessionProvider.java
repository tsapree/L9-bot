package biz.atomeo.l9.service;

import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LayeredSessionProvider implements SessionProvider {
    private final NewSessionProvider newSessionProvider;
    private final InMemorySessionProvider inMemorySessionProvider;
    private final FileStoreSessionProvider fileStoreSessionProvider;

    @Value("${l9.in-memory-cache-enabled}")
    private boolean IS_MEMORY_CACHE_ENABLED;

    @Override
    public SessionDTO getSession(Long chatId) throws L9Exception {
        SessionDTO session = null;
        if (IS_MEMORY_CACHE_ENABLED) {
            session = inMemorySessionProvider.getSession(chatId);
        }
        if (session == null) {
            session = fileStoreSessionProvider.getSession(chatId);
        }
        if (session == null) {
            session = newSessionProvider.getSession(chatId);
        }
        session.setLock(true);
        return session;
    }

    @Override
    public void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception {
        sessionDTO.setVersion(sessionDTO.getVersion() + 1);
        sessionDTO.setLock(false);
        sessionDTO.setUpdated(OffsetDateTime.now());

        if (IS_MEMORY_CACHE_ENABLED) {
            inMemorySessionProvider.updateSession(chatId, sessionDTO);
        } else {
            fileStoreSessionProvider.updateSession(chatId, sessionDTO);
        }
    }
}
