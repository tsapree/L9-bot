package biz.atomeo.l9.example;

public class Main {
    public static void main(String[] args) throws Exception {
        L9MiniConsoleApp l9MiniConsoleApp = new L9MiniConsoleApp();
        //GitHub won't place game resources to git repo, so for now no game file here
        l9MiniConsoleApp.startApp("games/EMERALD.SNA", null);
    }
}