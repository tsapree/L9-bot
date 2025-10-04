package biz.atomeo.l9.service;

import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class NewSessionProvider implements SessionProvider {
    @Override
    public SessionDTO getSession(Long chatId) throws L9Exception {
        return SessionDTO.builder()
                        .created(OffsetDateTime.now())
                        .updated(OffsetDateTime.now())
                        .chatState(ChatState.INIT)
                        .chatId(chatId)
                        .version(1L)
                        .build();
    }

    @Override
    public void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception {

    }
}
