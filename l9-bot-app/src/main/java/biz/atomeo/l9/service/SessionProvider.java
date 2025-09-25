package biz.atomeo.l9.service;

import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;

public interface SessionProvider {
    SessionDTO getSession(Long chatId) throws L9Exception;
    void updateSession(Long chatId, SessionDTO sessionDTO) throws L9Exception;
}
