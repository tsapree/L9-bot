package biz.atomeo.l9.chat;

import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.constants.L9Game;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import biz.atomeo.l9.service.L9GameFactory;
import biz.atomeo.l9.service.L9ReplyService;
import biz.atomeo.l9.utils.FileIOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("CHOOSE_GAME_HANDLER")
@RequiredArgsConstructor
public class ChooseGameHandler implements StateHandler {

    private final L9GameFactory gameFactory;
    private final L9ReplyService l9ReplyService;

    @Override
    public void onEnterState(AnswerDTO answer, SessionDTO session) {
        answer.appendText("""
                MENU:\s
                0. How to play\s
                \s
                GAMES:\s
                Silicon Dreams Trilogy:
                a. Snowball\s
                b. Return to Eden\s
                c. Worm in Paradise\s
                \s
                Individual games:
                d. Emerald Isle\s
                """);
    }

    @Override
    public void onCommand(String question, AnswerDTO answer, SessionDTO session) {
        try {
            switch (question) {
                case "0":
                    String help = FileIOUtils.loadTextFromResource("articles/how_to_play.md", "Error reading 'how to play' article.");
                    answer.appendText(help);
                    onEnterState(answer,session);
                    return;
                case "a":
                    gameFactory.startGame(session, L9Game.SNOWBALL_V3_PC);
                    break;
                case "b":
                    gameFactory.startGame(session, L9Game.RETURN_TO_EDEN_V3_PC);
                    break;
                case "c":
                    gameFactory.startGame(session, L9Game.WORM_IN_PARADISE_V3_PC);
                    break;
                case "d":
                    gameFactory.startGame(session, L9Game.EMERALD_ISLE_V2_S48);
                    break;

                default:
                    throw new L9Exception("Unknown game or load error.");
            }
            answer.setNewChatState(ChatState.PLAYING_GAME);
            answer.append(l9ReplyService.generateAnswer(session, " "));
        } catch (L9Exception e) {
            //TODO: отрефакторить процесс обработки ошибки при старте игры и при генерации ответа
            answer.appendText("Something went wrong.");
            onEnterState(answer, session);
            answer.setNewChatState(ChatState.CHOOSE_GAME);
        }
    }
}
