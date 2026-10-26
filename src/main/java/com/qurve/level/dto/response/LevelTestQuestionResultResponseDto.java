package com.qurve.level.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LevelTestQuestionResultResponseDto {
    private int questionId;
    private String questionText;
    private String difficulty;
    private int selectedOptionId;
    private String selectedOptionText;
    private int correctOptionId;
    private String correctOptionText;
    private boolean correct;

    public static LevelTestQuestionResultResponseDto from(LevelTestQuestionDto question, int selectedOptionId) {
        String selectedOptionText = findOptionText(question, selectedOptionId);
        String correctOptionText = findOptionText(question, question.getCorrectAnswer());

        return new LevelTestQuestionResultResponseDto(
                question.getQuestionId(),
                question.getQuestionText(),
                question.getDifficulty(),
                selectedOptionId,
                selectedOptionText,
                question.getCorrectAnswer(),
                correctOptionText,
                selectedOptionId == question.getCorrectAnswer()
        );
    }

    private static String findOptionText(LevelTestQuestionDto question, int optionId) {
        return question.getOptions().stream()
                .filter(option -> option.getOptionId() == optionId)
                .map(OptionDto::getText)
                .findFirst()
                .orElse("");
    }
}
