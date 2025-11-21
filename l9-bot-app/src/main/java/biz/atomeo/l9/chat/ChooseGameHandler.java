package biz.atomeo.l9.chat;

import biz.atomeo.l9.config.L9AppProperties;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.constants.L9Game;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import biz.atomeo.l9.service.L9GameFactory;
import biz.atomeo.l9.service.L9ReplyService;
import biz.atomeo.l9.utils.FileIOUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("CHOOSE_GAME_HANDLER")
@RequiredArgsConstructor
public class ChooseGameHandler implements StateHandler {

    private final L9GameFactory gameFactory;
    private final L9ReplyService l9ReplyService;
    private final L9AppProperties l9AppProperties;

    @Value("${l9.version}")
    private String botVersion;

    @Override
    public void onEnterState(AnswerDTO answer, SessionDTO session) {
        answer.appendText("L9 Games Bot "+botVersion+"\n\n"+
                """
                MENU:\s
                [0]. How to play\s
                \s
                GAMES:"""
                + l9AppProperties.getCatalogue().stream()
                .flatMap(cat -> Stream.concat(
                            Stream.of("\n"+cat.category()+":"),
                            cat.games().stream()
                                    .map(game -> "["+game.key()+"]. "+game.name())
                    )
                ).collect(Collectors.joining("\n"))
        );
    }

    @Override
    public void onCommand(String question, AnswerDTO answer, SessionDTO session) {
        try {
            if ("0".equalsIgnoreCase(question)) {
                String help = FileIOUtils.loadTextFromResource("articles/how_to_play.md", "Error reading 'how to play' article.");
                answer.appendText(help);
                onEnterState(answer, session);
                return;
            }

            Optional<L9Game> selectedGame = l9AppProperties.getCatalogue().stream()
                    .flatMap(cat -> cat.games().stream())
                    .filter(game -> game.key().equals(question))
                    .map(game -> EnumUtils.getEnum(L9Game.class, game.id()))
                    .filter(Objects::nonNull)
                    .findFirst();

            if (selectedGame.isPresent()) {
                gameFactory.startGame(session, selectedGame.get());
            } else {
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
