package biz.atomeo.l9.service;

import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySessionProvider implements SessionProvider {
    private final ConcurrentHashMap<Long, SessionDTO> sessions = new ConcurrentHashMap<>();

    @Override
    public SessionDTO getSession(Long chatId) throws L9Exception {
        return sessions.get(chatId);
    }

    @Override
    public void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception {
        sessions.put(chatId, sessionDTO);
    }

    public int getSessionsCount() {
        return sessions.size();
    }


    public List<Long> getActiveUserIds() {
        return new ArrayList<>(sessions.keySet());
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
    }
}
