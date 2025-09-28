package biz.atomeo.l9.service;

import biz.atomeo.l9.constants.L9Game;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final SessionProvider sessionProvider;
    private final BotStateProvider botState;
    private final L9GameFactory gameFactory;
    private final L9ReplyService l9ReplyService;

    @Value("${l9.version}")
    private String version;

    public AnswerDTO generateAnswer(Long chatId, String command) {
        if (!botState.isBotActive()) return AnswerDTO.builder()
                .answerText("Sorry, application is unavailable right now.")
                .build();

        try {
            SessionDTO session = sessionProvider.getSession(chatId);

            AnswerDTO response = doCommand(session, command);

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
        switch (session.getChatState()) {
            case INIT:
                session.setChatState(ChatState.CHOOSE_GAME);
                return AnswerDTO.builder()
                        .answerText(String.format("""
                       Welcome to L9 Games Bot v%s!\s
                       \s
                       """, version) + toChooseGame(session).getAnswerText())
                        .build();
            case PLAYING_GAME:
                AnswerDTO answerDTO = l9ReplyService.generateAnswer(session, command);
                if (ChatState.STOPPED_GAME.equals(answerDTO.getChatState())) {
                    answerDTO.append(toChooseGame(session));
                }
                return answerDTO;
            case CHOOSE_GAME:
            default:
                try {
                    return toPlayingGame(session, command);
                } catch (L9Exception e) {
                    return toChooseGame(session);
                }
        }
    }

    private AnswerDTO toChooseGame(SessionDTO session) {
        session.setChatState(ChatState.CHOOSE_GAME);
        return AnswerDTO.builder().answerText("""
                Please choose game to play:\s
                1. Emerald Isle\s
                2. Worm in Paradise\s
                3. Snowball\s
                """).build();
    }

    private AnswerDTO toPlayingGame(SessionDTO session, String command) throws L9Exception{
        switch (command) {
            case "1":
                gameFactory.startGame(session, L9Game.EMERALD_ISLE);
                break;
            case "2":
                gameFactory.startGame(session, L9Game.WORM_PC);
                break;
            case "3":
                gameFactory.startGame(session, L9Game.SNOWBALL);
                break;
            default:
                throw new L9Exception("Unknown game or load error.");
        }
        session.setChatState(ChatState.PLAYING_GAME);
        return l9ReplyService.generateAnswer(session, " ");
    }
}
