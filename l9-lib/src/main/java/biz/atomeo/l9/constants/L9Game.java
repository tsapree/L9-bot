package biz.atomeo.l9.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum L9Game {
    EMERALD_ISLE_V2_S48("eiv2s48"),

    //Silicon Dreams Trilogy
    SNOWBALL_V3_PC("sv3pc"),
    RETURN_TO_EDEN_V3_PC("rtev3pc"),
    WORM_IN_PARADISE_V3_PC("wipv3pc"),
    ;

    private final String id;
}
