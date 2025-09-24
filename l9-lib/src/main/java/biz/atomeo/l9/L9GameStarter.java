package biz.atomeo.l9;

public class L9GameStarter {
    public static L9GameService buildGame(L9Game game) {
        L9GameService service = new L9GameService(game);
        return service;
    }
}
