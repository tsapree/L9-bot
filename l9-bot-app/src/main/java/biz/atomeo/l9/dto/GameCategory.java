package biz.atomeo.l9.dto;

import java.util.List;

public record GameCategory(
    String category,
    List<GameInfo> games
) {}
