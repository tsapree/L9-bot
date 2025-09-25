package biz.atomeo.l9;

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
