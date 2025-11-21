package biz.atomeo.l9.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum L9Game {
    //The Middle-Earth Trilogy / Jewels of Darkness
    COLOSSAL_ADVENTURE_V3_S48("cav3s48"),
    ADVENTURE_QUEST_V3_S48("aqv4s48"),
    DUNGEON_ADVENTURE_V3_S48("dav3s48"),

    //Silicon Dreams Trilogy
    SNOWBALL_V3_PC("sv3pc"),
    RETURN_TO_EDEN_V3_PC("rtev3pc"),
    WORM_IN_PARADISE_V3_PC("wipv3pc"),

    //The Time and Magik Trilogy
    LORDS_OF_TIME_V4_S48("lotv4s48"),
    RED_MOON_V2_S48("rmv2s48"),
    PRICE_OF_MAGIK_V3_S48("pomv3s48"),

    //Individual games
    EMERALD_ISLE_V2_S48("eiv2s48"),
    ;

    private final String id;
}
