package biz.atomeo.l9;

import lombok.Getter;

@Getter
public enum L9Game {
    EMERALD_ISLE("games/EMERALD.SNA");

    private final String gamePath;
    private final String picturesPath;

    L9Game(String path) {
        this.gamePath = path;
        this.picturesPath = null;
    }
}
