package biz.atomeo.l9.service;

import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
        session.setUpdated(OffsetDateTime.now());
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

    public int getActiveUsers() {
        return inMemorySessionProvider.getSessionsCount();
    }

    public void parkSessions(int minutesBeforeSave) {
        List<Long> ids = inMemorySessionProvider.getActiveChatIds();
        log.debug("Parking sessions older than {} minutes. Found {} sessions in memory cache.",
                minutesBeforeSave, ids.size());
        ids.forEach(id -> {
            SessionDTO session = null;
            try {
                session = inMemorySessionProvider.getSession(id);
                if (!session.isLock()
                        && session.getUpdated().isBefore(OffsetDateTime.now().minusMinutes(minutesBeforeSave))) {
                    session.setLock(true);
                    log.debug("Park session "+id);
                    fileStoreSessionProvider.updateSession(id, session);
                    session.setLock(false);
                    inMemorySessionProvider.removeSession(id);
                }
            } catch (L9Exception e) {
                log.error("Error while trying to park session {}:", id, e);
                if (session!=null) {
                    if (session.isLock()) session.setLock(false);
                }
            }
        });
    }
}
