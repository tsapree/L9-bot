package biz.atomeo.l9.chat;

import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;

public interface StateHandler {
    default void onEnterState(AnswerDTO answer, SessionDTO session) {}
    default void onCommand(String question, AnswerDTO answer, SessionDTO session) {}
}
