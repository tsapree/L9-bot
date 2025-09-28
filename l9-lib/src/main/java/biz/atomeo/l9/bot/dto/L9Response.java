package biz.atomeo.l9.bot.dto;

import biz.atomeo.l9.constants.L9Phase;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class L9Response {
    private L9Phase phase;
    private String status;
    private List<String> pictures;
    private String message;
}
