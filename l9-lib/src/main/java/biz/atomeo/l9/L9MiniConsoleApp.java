package biz.atomeo.l9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class L9MiniConsoleApp {

    private L9BotConnector connector;

    public L9MiniConsoleApp() {
        TextOutputAdapter textOutputAdapter = new TextOutputAdapter() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void printChar(char c) {
                if (c!='\r') sb.append(c);
                else sb.append('\n');
            }

            @Override
            public void flush() {
                System.out.println(sb.toString());
                sb.delete(0, sb.length());
            }
        };

        InputAdapter inputAdapter = () -> {
            try {
                return new BufferedReader(new InputStreamReader(System.in)).readLine();
            } catch (IOException e) {
                return "";
            }
        };

        IOAdapter ioAdapter = L9MiniConsoleApp::loadResourceAsBytes;

        connector = new L9BotConnector(textOutputAdapter, inputAdapter, ioAdapter);
    }

    public void startApp(String gamePath, String picName) {
        if (connector.LoadGame(gamePath, picName)!=true) {
            //
        }
        while ((connector.L9State != connector.L9StateStopped)) {
            if (connector.L9State == connector.L9StateWaitForCommand) {
                connector.InputCommand(connector.inputAdapter.inputCommand());
            } else step();
        };
    }

    void step() {
        while (connector.L9State==connector.L9StateRunning
                || connector.L9State==connector.L9StateCommandReady)
            connector.RunGame();
    };

    public static byte[] loadResourceAsBytes(String resourcePath) {
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
