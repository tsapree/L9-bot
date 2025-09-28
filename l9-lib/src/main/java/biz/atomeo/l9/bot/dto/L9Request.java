package biz.atomeo.l9.bot.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class L9Request {
    private String command;
    private String key;
}
