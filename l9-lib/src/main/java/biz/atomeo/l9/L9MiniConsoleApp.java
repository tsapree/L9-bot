package biz.atomeo.l9;

import biz.atomeo.l9.legacy.L9Picture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class L9MiniConsoleApp {

    private final L9BotConnector connector;

    TextOutputAdapter textOutputAdapter = new TextOutputAdapter() {
        final StringBuilder sb = new StringBuilder();

        @Override
        public void printChar(char c) {
            if (c=='\r') sb.append('\n');
            else sb.append(c);
        }

        @Override
        public void flush() {
        }

        @Override
        public String getMessage() {
            String message = sb.toString();
            sb.delete(0, sb.length());
            return message;
        }
    };

    public L9MiniConsoleApp() {

        InputAdapter inputAdapter = new InputAdapter() {
            @Override
            public String inputCommand() {
                try {
                    return new BufferedReader(new InputStreamReader(System.in)).readLine();
                } catch (IOException e) {
                    return "";
                }
            }

            @Override
            public String inputKey() {
                try {
                    String key = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    if (key.isEmpty()) return "\n";
                    else return key.substring(0, 1);
                } catch (IOException e) {
                    return "";
                }
            }
        };

        IOAdapter ioAdapter = new IOAdapter() {
            @Override
            public byte[] loadFile(String fileName) {
                return loadResourceAsBytes(fileName);
            }

            @Override
            public String getGamePath(L9Game game) {
                return "games/EMERALD.SNA";
            }

            @Override
            public String getPicPath(L9Game game) {
                return null;
            }

            @Override
            public boolean fileExist(String file) {
                return false;
            }

            @Override
            public byte[] fileLoadRelativeToArray(String file) {
                return null;
            }

            @Override
            public boolean isPictureCached(int picture) {
                return false;
            }

            @Override
            public void cachePicture(int picture, List<L9Picture> pictures) {

            }

            @Override
            public Object popPictures() {
                return null;
            }
        };

        connector = new L9BotConnector(textOutputAdapter, inputAdapter, ioAdapter);
    }

    public void startApp(String gamePath, String picName) {
        if (connector.LoadGame(gamePath, picName)!=true) {
            //
        }
        while ((connector.L9State != connector.L9StateStopped)) {
            if (connector.L9State == connector.L9StateWaitForCommand) {
                System.out.println(textOutputAdapter.getMessage());
                connector.InputCommand(connector.inputAdapter.inputCommand());
            } else step();
        };
    }

    void step() {
        while (connector.L9State==connector.L9StateRunning
                || connector.L9State==connector.L9StateCommandReady)
            connector.RunGame();
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
