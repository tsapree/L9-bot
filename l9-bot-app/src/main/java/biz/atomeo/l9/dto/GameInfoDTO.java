package biz.atomeo.l9.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class GameInfoDTO {
    private final String archive;
    private final String folder;
    private final String path;
    private String pic;
}
