package biz.atomeo.l9.service;

import biz.atomeo.l9.openapi.model.L9StatRs;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotStateProvider {
    private final InMemorySessionProvider inMemorySessionProvider;

    private boolean enabled = true;

    @Getter
    private L9StatRs.BotStatusEnum status = L9StatRs.BotStatusEnum.STARTING;

    @PostConstruct
    private void started() {
        log.info("Bot state provider started");
        enableBot();
    }

    public boolean isBotActive() {
        return enabled;
    }

    public void disableBot() {
        if (enabled) {
            enabled = false;
            status = L9StatRs.BotStatusEnum.SHUTTING_DOWN;
            //TODO
            status = L9StatRs.BotStatusEnum.DISABLED;
        }
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
