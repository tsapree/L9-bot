package biz.atomeo.l9;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        L9MiniConsoleApp l9MiniConsoleApp = new L9MiniConsoleApp();
        //Github won't place game resources to git repo, so for now no game file here
        l9MiniConsoleApp.startApp("games/EMERALD.SNA", null);
    }
}