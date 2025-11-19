package biz.atomeo.l9.service;

import biz.atomeo.l9.config.L9AppProperties;
import biz.atomeo.l9.constants.L9Game;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final LayeredSessionProvider sessionProvider;
    private final BotStateProvider botState;
    private final L9GameFactory gameFactory;
    private final L9ReplyService l9ReplyService;
    private final L9AppProperties l9AppProperties;

    @Value("${l9.version}")
    private String botVersion;

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
                       """, botVersion) + toChooseGame(session).getAnswerText())
                        .build();
            case PLAYING_GAME:
                if ("#about".equalsIgnoreCase(command.trim())) {
                    return AnswerDTO.builder()
                            .answerText("Please visit site for more information about game: "
                                    +l9AppProperties.getGames().get(session.getGameState().getL9game().name()).about()
                            )
                            .build();
                }
                if ("#howtoplay".equalsIgnoreCase(command.trim())) {
                    String help = null;
                    try {
                        InputStream resource = new ClassPathResource(
                                "how_to_play.md").getInputStream();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(resource))) {
                            help = reader.lines()
                                    .collect(Collectors.joining("\n"));

                        }
                    } catch (IOException e) {
                        help = "Error reading how to play";
                    }
                    return AnswerDTO.builder()
                            .answerText(help)
                            .build();
                }
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
                gameFactory.startGame(session, L9Game.EMERALD_ISLE_V2_S48);
                break;
            case "2":
                gameFactory.startGame(session, L9Game.WORM_IN_PARADISE_V3_PC);
                break;
            case "3":
                gameFactory.startGame(session, L9Game.SNOWBALL_V3_PC);
                break;
            default:
                throw new L9Exception("Unknown game or load error.");
        }
        session.setChatState(ChatState.PLAYING_GAME);
        return l9ReplyService.generateAnswer(session, " ");
    }
}
