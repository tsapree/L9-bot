package biz.atomeo.l9;

import java.io.IOException;
import java.io.InputStream;

import static biz.atomeo.l9.legacy.L9.*;

public class L9GameService {
    //Хочу держать здесь все, что касается конкретной игры
    // - файлы
    // - настройки vm
    //В идеале боту нужно будет по одному контейнеру для каждой игры. Независимо от количества юзеров
    private final L9BotConnector connector;
    private final L9Game l9game;

    //TODO: change it, it's lame
    private final L9Request l9request = new L9Request();

    public L9GameService(L9Game l9game) {
        this.l9game = l9game;

        IOAdapter ioAdapter = L9GameService::loadResourceAsBytes;
        InputAdapter inputAdapter = new InputAdapter() {
            @Override
            public String inputCommand() {
                return l9request.getCommand();
            }

            @Override
            public String inputKey() {
                String key = l9request.getKey();
                if (key==null || key.isEmpty()) return "\n";
                else return key.substring(0, 1);
            }
        };
        connector = new L9BotConnector(textOutputAdapter, inputAdapter, ioAdapter);

        if (!connector.LoadGame(l9game.getGamePath(), l9game.getPicturesPath())) {
            throw new RuntimeException("Failed to load game.");
        }
    }

    public L9Response doStep(L9Request request, L9GameState state) {
        //TODO: только потому что пока не придумал как передавать данные лучше
        // чем через обратный вызов, который хранит ссылку на объекты
        //TODO: валидировать rq согласно стейта

        this.l9request.setKey(request.getKey());
        this.l9request.setCommand(request.getCommand());

        if (connector.L9State == connector.L9StateWaitForCommand) {
            connector.InputCommand(request.getCommand());
        }

        while (connector.L9State == connector.L9StateRunning
        || connector.L9State == connector.L9StateCommandReady
        || connector.L9State == connector.L9StateKeyReady) {
            //TODO: чтобы точно не было зависания - выполнять шаги порциями
            step();
        }

        L9Phase phase = switch (connector.L9State) {
            case L9StateStopped -> L9Phase.STOPPED;
            case L9StateWaitForCommand -> L9Phase.WAITING_FOR_A_COMMAND;
            case L9StateWaitForKey -> L9Phase.WAITING_FOR_A_KEY;
            default -> null; //TODO: не должно быть такого, но на всякий
        };

        return L9Response.builder()
                .phase(phase)
                .status("Success")
                .message(textOutputAdapter.getMessage())
                .build();
    }

    private void step() {
        while (connector.L9State == connector.L9StateRunning
                || connector.L9State == connector.L9StateCommandReady)
            connector.RunGame();
    };

    private TextOutputAdapter textOutputAdapter = new TextOutputAdapter() {
        final StringBuilder sb = new StringBuilder();

        @Override
        public void printChar(char c) {
            if (c=='\r') sb.append('\n');
            else sb.append(c);
        }

        @Override
        public void flush() {
        }

        public String getMessage() {
            String message = sb.toString();
            sb.delete(0, sb.length());
            return message;
        }
    };

    private static byte[] loadResourceAsBytes(String resourcePath) {
        try (InputStream is = L9MiniConsoleApp.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return is.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
