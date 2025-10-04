package biz.atomeo.l9.service;

import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import biz.atomeo.l9.openapi.model.L9StatRs;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotStateProvider {
    private final InMemorySessionProvider inMemorySessionProvider;
    private final FileStoreSessionProvider fileStoreSessionProvider;

    private boolean enabled = false;

    @Getter
    private L9StatRs.BotStatusEnum status = null;

    @PostConstruct
    private void started() {
        log.info("Bot state provider started");
        enableBot();
    }

    public boolean isBotActive() {
        return enabled;
    }

    public void disableBot() {
        enabled = false;

        status = L9StatRs.BotStatusEnum.SHUTTING_DOWN;
        parkSessions();
        status = L9StatRs.BotStatusEnum.DISABLED;
    }

    private void parkSessions() {
        List<Long> ids = inMemorySessionProvider.getActiveUserIds();
        ids.forEach(id -> {
            SessionDTO session = null;
            try {
                session = inMemorySessionProvider.getSession(id);
                if (!session.isLock()) {
                    session.setLock(true);
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

    public void enableBot() {
        if (!enabled) {
            status = L9StatRs.BotStatusEnum.STARTING;
            //TODO
            status = L9StatRs.BotStatusEnum.ENABLED;
            enabled = true;
        }
    }

    public int getActiveUsers() {
        return inMemorySessionProvider.getSessionsCount();
    }
}
