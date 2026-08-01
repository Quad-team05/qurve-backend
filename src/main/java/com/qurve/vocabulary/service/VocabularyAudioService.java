package com.qurve.vocabulary.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.vocabulary.domain.VocabularyWord;
import com.qurve.vocabulary.repository.VocabularyWordRepository;
import com.qurve.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
public class VocabularyAudioService {

    private final UserRepository userRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;
    private final String language;
    private final String codec;
    private final String format;

    public VocabularyAudioService(
            UserRepository userRepository,
            VocabularyWordRepository vocabularyWordRepository,
            @Value("${tts.voicerss.base-url:https://api.voicerss.org}") String voiceRssBaseUrl,
            @Value("${tts.voicerss.api-key:}") String apiKey,
            @Value("${tts.voicerss.language:ja-jp}") String language,
            @Value("${tts.voicerss.codec:MP3}") String codec,
            @Value("${tts.voicerss.format:44khz_16bit_stereo}") String format
    ) {
        this.userRepository = userRepository;
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.baseUrl = voiceRssBaseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(voiceRssBaseUrl)
                .build();
        this.apiKey = apiKey;
        this.language = language;
        this.codec = codec;
        this.format = format;
    }

    @PostConstruct
    public void logConfiguration() {
        log.info(
                "VoiceRSS config loaded. baseUrl={}, apiKeyPresent={}, language={}, codec={}, format={}",
                baseUrl,
                StringUtils.hasText(apiKey),
                language,
                codec,
                format
        );
    }

    /**
     * 단어 음성 조회
     *
     * * 단어 학습 화면에서 일본어 발음을 들을 수 있도록
     * 외부 TTS API에서 생성한 음성 데이터를 반환한다.
     *
     * @param loginId 로그인 ID
     * @param wordId 음성을 조회할 단어 ID
     * @return MP3 음성 데이터
     * @throws BusinessException 유저, 단어, 외부 음성 API 정보가 유효하지 않은 경우
     */
    public byte[] findWordAudio(String loginId, Long wordId) {
        validateUser(loginId);

        if (!StringUtils.hasText(apiKey)) {
            log.warn("VoiceRSS API key is missing at runtime.");
            throw new BusinessException(ErrorCode.VOCABULARY_AUDIO_FAIL);
        }

        VocabularyWord word = vocabularyWordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_WORD_NOT_FOUND));

        byte[] audio = requestVoiceRssAudio(normalizeTextForSpeech(word.getExpression()));

        if (audio.length == 0 || isVoiceRssError(audio)) {
            log.warn("VoiceRSS returned a non-audio response for wordId={}. body={}", wordId, toUtf8Text(audio));
            throw new BusinessException(ErrorCode.VOCABULARY_AUDIO_FAIL);
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
                            .queryParam("src", text)
                            .queryParam("c", codec)
                            .queryParam("f", format)
                            .build())
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
                throw new BusinessException(ErrorCode.VOCABULARY_AUDIO_FAIL);
            }

            return audio;
        } catch (RestClientResponseException e) {
            log.warn(
                    "VoiceRSS HTTP error. status={}, body={}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new BusinessException(ErrorCode.VOCABULARY_AUDIO_FAIL);
        } catch (RestClientException e) {
            log.warn("VoiceRSS request failed. message={}", e.getMessage());
            throw new BusinessException(ErrorCode.VOCABULARY_AUDIO_FAIL);
        }
    }

    private String normalizeTextForSpeech(String expression) {
        return expression.split(";")[0].trim();
    }

    private boolean isVoiceRssError(byte[] audio) {
        String responseText = toUtf8Text(audio);
        return responseText.startsWith("ERROR:");
    }

    private String toUtf8Text(byte[] audio) {
        return new String(audio, StandardCharsets.UTF_8);
    }
}
