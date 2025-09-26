package biz.atomeo.l9.service;

import org.springframework.stereotype.Component;

@Component
public class BotStateProvider {
    public boolean isBotActive() {
        return true; //TODO: on/off realization
    }
}
