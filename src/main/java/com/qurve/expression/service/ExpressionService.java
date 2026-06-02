package com.qurve.expression.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qurve.expression.dto.response.TodayExpressionResponseDto;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
public class ExpressionService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final String TATOEBA_SENTENCE_URL_FORMAT = "https://tatoeba.org/ko/sentences/show/%d";

    private final UserRepository userRepository;
    private final RestClient restClient;

    public ExpressionService(
            UserRepository userRepository,
            @Value("${tatoeba.api.base-url:https://api.tatoeba.org}") String tatoebaBaseUrl
    ) {
        this.userRepository = userRepository;
        this.restClient = RestClient.builder()
                .baseUrl(tatoebaBaseUrl)
                .build();
    }

    /**
     * 오늘의 표현 조회
     *
     * * Tatoeba API에서 짧은 일본어 문장과 한국어 번역 목록을 조회한 뒤,
     * 오늘 날짜를 기준으로 하루에 하나의 표현을 선택해 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 오늘의 표현 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않거나 표현 조회에 실패한 경우
     */
    public TodayExpressionResponseDto findTodayExpression(String loginId) {
        validateUser(loginId);

        JsonNode response = requestTatoebaSentences();
        JsonNode sentences = response.path("data");

        if (!sentences.isArray() || sentences.isEmpty()) {
            throw new BusinessException(ErrorCode.TODAY_EXPRESSION_NOT_FOUND);
        }

        JsonNode todaySentence = sentences.get(calculateTodayIndex(sentences.size()));
        JsonNode translation = findKoreanTranslation(todaySentence);

        if (translation == null) {
            throw new BusinessException(ErrorCode.TODAY_EXPRESSION_NOT_FOUND);
        }

        long sentenceId = todaySentence.path("id").asLong();

        return TodayExpressionResponseDto.of(
                sentenceId,
                todaySentence.path("text").asText(),
                translation.path("text").asText(),
                String.format(TATOEBA_SENTENCE_URL_FORMAT, sentenceId),
                todaySentence.path("license").asText()
        );
    }

    private void validateUser(String loginId) {
        if (!userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
    // Tatoeba에서 한국어 번역이 있는 짧은 일본어 문장 100개를 가져옴
    private JsonNode requestTatoebaSentences() {
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/sentences")
                            .queryParam("lang", "jpn")
                            .queryParam("trans:lang", "kor")
                            .queryParam("trans:is_direct", "yes")
                            .queryParam("trans:is_unapproved", "no")
                            .queryParam("is_unapproved", "no")
                            .queryParam("is_orphan", "no")
                            .queryParam("word_count", "4-20")
                            .queryParam("sort", "words")
                            .queryParam("limit", "100")
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.TATOEBA_API_FAIL);
            }

            return response;
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.TATOEBA_API_FAIL);
        }
    }

    private int calculateTodayIndex(int sentenceCount) {
        long today = LocalDate.now(KST_ZONE).toEpochDay();
        return (int) (today % sentenceCount);
    }

    private JsonNode findKoreanTranslation(JsonNode sentence) {
        JsonNode translations = sentence.path("translations");

        if (!translations.isArray()) {
            return null;
        }

        for (JsonNode translation : translations) {
            if ("kor".equals(translation.path("lang").asText())) {
                return translation;
            }
        }

        return null;
    }
}
