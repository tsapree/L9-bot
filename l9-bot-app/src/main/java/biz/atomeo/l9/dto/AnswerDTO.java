package biz.atomeo.l9.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AnswerDTO {
    private List<String> picturesFilenames;
    private String answerText;
}
