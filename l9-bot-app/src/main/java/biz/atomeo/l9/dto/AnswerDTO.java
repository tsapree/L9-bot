package biz.atomeo.l9.dto;

import biz.atomeo.l9.constants.ChatState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AnswerDTO {
    private List<String> picturesFilenames;
    private String answerText;
    private ChatState chatState;

    public void appendText(String text) {
        if (text!=null) {
            answerText = answerText == null ? text : answerText + text;
        }
    }

    public void addPictures(List<String> pictures) {
        if (pictures != null) {
            if (picturesFilenames == null) {
                picturesFilenames = pictures;
            } else {
                picturesFilenames.addAll(pictures);
            }
        }
    }

    public void append(AnswerDTO anotherAnswerDTO) {
        appendText(anotherAnswerDTO.getAnswerText());
        addPictures(anotherAnswerDTO.getPicturesFilenames());
    }
}
