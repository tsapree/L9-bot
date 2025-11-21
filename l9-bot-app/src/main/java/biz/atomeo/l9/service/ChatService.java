package biz.atomeo.l9.service;

import biz.atomeo.l9.chat.StateHandler;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final LayeredSessionProvider sessionProvider;
    private final BotStateProvider botState;

    private final Map<String,StateHandler> stateHandlers;

    public AnswerDTO generateAnswer(Long chatId, String command) {
        if (!botState.isBotActive()) return AnswerDTO.builder()
                .answerText("Sorry, application is unavailable right now.")
                .build();

        try {
            SessionDTO session = sessionProvider.getSession(chatId);

            AnswerDTO response = doCommand(session, command);

            if (response.getNewChatState() != null) {
                session.setChatState(response.getNewChatState());
            }
            sessionProvider.updateSession(chatId, session);
            return response;
        } catch (L9Exception e) {
            log.error("Error generating message:", e);
            return AnswerDTO.builder()
                    .answerText("Sorry, something went wrong.")
                    .build();
        }
    }

    private AnswerDTO doCommand(SessionDTO session, String command) {
        ChatState state = session.getChatState();
        AnswerDTO answer = AnswerDTO.builder()
                .build();

        getStateHandler(state).onCommand(command, answer, session);

        ChatState newState = answer.getNewChatState();
        if (newState!=null && newState != state) {
            getStateHandler(newState).onEnterState(answer, session);
        }
        return answer;
    }

    private StateHandler getStateHandler(ChatState state) {
        if (state == null) state = ChatState.INIT;
        //TODO: NPE?
        return stateHandlers.get(state.name()+"_HANDLER");
    }
}
