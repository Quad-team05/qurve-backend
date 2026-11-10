package com.qurve.expression.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qurve.expression.dto.response.TodayExpressionResponseDto;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ExpressionService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final String TATOEBA_SENTENCE_URL_FORMAT = "https://tatoeba.org/ko/sentences/show/%d";
    private static final String QURVE_SOURCE_URL = "https://qurve.duckdns.org";

    // Keep the home card available when the external sentence search is unavailable.
    private static final List<TodayExpression> ENGLISH_FALLBACK_EXPRESSIONS = List.of(
            new TodayExpression("How was your day?", "오늘 하루 어땠어요?"),
            new TodayExpression("Could you help me, please?", "도와주시겠어요?"),
            new TodayExpression("I am looking forward to it.", "기대하고 있어요."),
            new TodayExpression("What do you recommend?", "무엇을 추천하시나요?"),
            new TodayExpression("That sounds like a good idea.", "좋은 생각인 것 같아요."),
            new TodayExpression("Could you say that again?", "다시 말씀해 주시겠어요?"),
            new TodayExpression("I will be there in ten minutes.", "10분 안에 도착할게요.")
    );
    private static final List<TodayExpression> JAPANESE_FALLBACK_EXPRESSIONS = List.of(
            new TodayExpression("今日はどうでしたか。", "오늘은 어땠어요?"),
            new TodayExpression("手伝ってもらえますか。", "도와주실 수 있나요?"),
            new TodayExpression("楽しみにしています。", "기대하고 있어요."),
            new TodayExpression("おすすめは何ですか。", "추천은 무엇인가요?"),
            new TodayExpression("いい考えですね。", "좋은 생각이네요."),
            new TodayExpression("もう一度言ってもらえますか。", "한 번 더 말해 주실 수 있나요?"),
            new TodayExpression("十分後に着きます。", "10분 후에 도착해요.")
    );

    private final UserRepository userRepository;
    private final RestClient restClient;

    public ExpressionService(
            UserRepository userRepository,
            @Value("${tatoeba.api.base-url:https://api.tatoeba.org}") String tatoebaBaseUrl,
            @Value("${tatoeba.api.connect-timeout-millis:3000}") int connectTimeoutMillis,
            @Value("${tatoeba.api.read-timeout-millis:5000}") int readTimeoutMillis
    ) {
        this.userRepository = userRepository;
        this.restClient = RestClient.builder()
                .baseUrl(tatoebaBaseUrl)
                .requestFactory(createRequestFactory(connectTimeoutMillis, readTimeoutMillis))
                .build();
    }

    /**
     * 오늘의 표현 조회
     *
     * * Tatoeba API에서 사용자의 학습 언어에 맞는 짧은 문장과 한국어 번역 목록을 조회한 뒤,
     * 오늘 날짜를 기준으로 하루에 하나의 표현을 선택해 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 오늘의 표현 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않거나 표현 조회에 실패한 경우
     */
    public TodayExpressionResponseDto findTodayExpression(String loginId) {
        User user = findUser(loginId);

        try {
            JsonNode response = requestTatoebaSentences(user.getLearningLanguage());
            JsonNode sentences = response.path("data");

            if (!sentences.isArray() || sentences.isEmpty()) {
                return createFallbackExpression(user.getLearningLanguage());
            }

            JsonNode todaySentence = sentences.get(calculateTodayIndex(sentences.size()));
            JsonNode translation = findKoreanTranslation(todaySentence);

            if (translation == null) {
                return createFallbackExpression(user.getLearningLanguage());
            }

            long sentenceId = todaySentence.path("id").asLong();

            return TodayExpressionResponseDto.of(
                    sentenceId,
                    todaySentence.path("text").asText(),
                    translation.path("text").asText(),
                    String.format(TATOEBA_SENTENCE_URL_FORMAT, sentenceId),
                    todaySentence.path("license").asText(),
                    user.getLearningLanguage()
            );
        } catch (BusinessException e) {
            if (e.getErrorCode() != ErrorCode.TATOEBA_API_FAIL) {
                throw e;
            }

            log.warn("Tatoeba is unavailable; returning a built-in daily expression. language={}", user.getLearningLanguage());
            return createFallbackExpression(user.getLearningLanguage());
        }
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // Tatoeba에서 한국어 번역이 있는 짧은 학습 언어 문장 100개를 가져옵니다.
    private JsonNode requestTatoebaSentences(LearningLanguage learningLanguage) {
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/sentences")
                            .queryParam("lang", toTatoebaLanguageCode(learningLanguage))
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

    private String toTatoebaLanguageCode(LearningLanguage learningLanguage) {
        return learningLanguage == LearningLanguage.ENGLISH ? "eng" : "jpn";
    }

    private int calculateTodayIndex(int sentenceCount) {
        long today = LocalDate.now(KST_ZONE).toEpochDay();
        return (int) (today % sentenceCount);
    }

    private TodayExpressionResponseDto createFallbackExpression(LearningLanguage learningLanguage) {
        List<TodayExpression> expressions = learningLanguage == LearningLanguage.ENGLISH
                ? ENGLISH_FALLBACK_EXPRESSIONS
                : JAPANESE_FALLBACK_EXPRESSIONS;
        TodayExpression expression = expressions.get(calculateTodayIndex(expressions.size()));

        return TodayExpressionResponseDto.of(
                null,
                expression.text(),
                expression.koreanTranslation(),
                QURVE_SOURCE_URL,
                "QURVE",
                learningLanguage
        );
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

    private SimpleClientHttpRequestFactory createRequestFactory(int connectTimeoutMillis, int readTimeoutMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);
        return requestFactory;
    }

    private record TodayExpression(String text, String koreanTranslation) {
    }
}
