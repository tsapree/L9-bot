package biz.atomeo.l9;

public class L9GameStarter {
    public static L9GameService buildGame(L9Game game, IOAdapter ioAdapter) {
        L9GameService service = new L9GameService(game, ioAdapter);
        return service;
    }
}
