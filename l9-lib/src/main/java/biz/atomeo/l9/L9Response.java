package biz.atomeo.l9;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class L9Response {
    private L9Phase phase;
    private String status;
    private String picture;
    private String message;
}
