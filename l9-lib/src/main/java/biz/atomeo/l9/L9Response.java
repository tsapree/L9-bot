package biz.atomeo.l9;

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
