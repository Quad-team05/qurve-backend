package com.qurve.expression.service;

import com.qurve.expression.domain.BasicExpression;
import com.qurve.expression.repository.BasicExpressionRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@Transactional(readOnly = true)
public class BasicExpressionAudioService {

    private final UserRepository userRepository;
    private final BasicExpressionRepository basicExpressionRepository;
    private final RestClient restClient;
    private final String apiKey;
    private final String language;
    private final String codec;
    private final String format;

    public BasicExpressionAudioService(
            UserRepository userRepository,
            BasicExpressionRepository basicExpressionRepository,
            @Value("${tts.voicerss.base-url:https://api.voicerss.org}") String voiceRssBaseUrl,
            @Value("${tts.voicerss.api-key:}") String apiKey,
            @Value("${tts.voicerss.language:ja-jp}") String language,
            @Value("${tts.voicerss.codec:MP3}") String codec,
            @Value("${tts.voicerss.format:44khz_16bit_stereo}") String format,
            @Value("${tts.voicerss.connect-timeout-millis:3000}") int connectTimeoutMillis,
            @Value("${tts.voicerss.read-timeout-millis:5000}") int readTimeoutMillis
    ) {
        this.userRepository = userRepository;
        this.basicExpressionRepository = basicExpressionRepository;
        this.restClient = RestClient.builder()
                .baseUrl(voiceRssBaseUrl)
                .requestFactory(createRequestFactory(connectTimeoutMillis, readTimeoutMillis))
                .build();
        this.apiKey = apiKey;
        this.language = language;
        this.codec = codec;
        this.format = format;
    }

    /**
     * 초급 표현 음성 조회
     *
     * * 선택한 초급 일본어 표현을 VoiceRSS로 변환해 MP3 음성 데이터를 반환한다.
     *
     * @param loginId 로그인 ID
     * @param expressionId 음성을 조회할 표현 ID
     * @return MP3 음성 데이터
     * @throws BusinessException 유저, 표현 또는 음성 API 정보가 유효하지 않은 경우
     */
    public byte[] findBasicExpressionAudio(String loginId, Long expressionId) {
        validateUser(loginId);

        BasicExpression expression = basicExpressionRepository
                .findById(expressionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BASIC_EXPRESSION_NOT_FOUND));

        if (!StringUtils.hasText(apiKey)) {
            log.warn("VoiceRSS API key is missing at runtime.");
            throw new BusinessException(ErrorCode.BASIC_EXPRESSION_AUDIO_FAIL);
        }

        byte[] audio = requestVoiceRssAudio(expression.getKanji().trim());

        if (audio.length == 0 || isVoiceRssError(audio)) {
            log.warn("VoiceRSS returned an empty or error response for expressionId={}.", expressionId);
            throw new BusinessException(ErrorCode.BASIC_EXPRESSION_AUDIO_FAIL);
        }

        return audio;
    }

    private void validateUser(String loginId) {
        if (!userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private byte[] requestVoiceRssAudio(String text) {
        try {
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("key", apiKey)
                            .queryParam("hl", language)
                            .queryParam("src", "{text}")
                            .queryParam("c", codec)
                            .queryParam("f", format)
                            .build(text))
                    .retrieve()
                    .toEntity(byte[].class);

            log.info(
                    "VoiceRSS response received. status={}, contentType={}, textLength={}",
                    response.getStatusCode().value(),
                    response.getHeaders().getContentType(),
                    text.length()
            );

            byte[] audio = response.getBody();

            if (audio == null) {
                log.warn("VoiceRSS response body is null.");
                throw new BusinessException(ErrorCode.BASIC_EXPRESSION_AUDIO_FAIL);
            }

            return audio;
        } catch (RestClientResponseException e) {
            log.warn("VoiceRSS HTTP error. status={}", e.getStatusCode().value());
            throw new BusinessException(ErrorCode.BASIC_EXPRESSION_AUDIO_FAIL);
        } catch (RestClientException e) {
            log.warn("VoiceRSS request failed. exceptionType={}", e.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.BASIC_EXPRESSION_AUDIO_FAIL);
        }
    }

    private boolean isVoiceRssError(byte[] audio) {
        String responseText = new String(audio, StandardCharsets.UTF_8);

        return responseText.stripLeading().startsWith("ERROR:");
    }

    private SimpleClientHttpRequestFactory createRequestFactory(int connectTimeoutMillis, int readTimeoutMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);

        return requestFactory;
    }
}