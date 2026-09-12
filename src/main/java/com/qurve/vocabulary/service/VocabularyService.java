package com.qurve.vocabulary.service;

import com.qurve.attendance.service.AttendanceService;
import com.qurve.badge.service.BadgeService;
import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.challenge.service.ChallengeProgressService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.enums.XpActionType;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.xp.service.XpService;
import com.qurve.vocabulary.domain.Bookmark;
import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.vocabulary.domain.UserWordStudy;
import com.qurve.vocabulary.domain.VocabularyWord;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordStudyResponseDto;
import com.qurve.vocabulary.dto.request.ChallengeWordCompleteRequestDto;
import com.qurve.vocabulary.dto.response.ChallengeWordCompleteResponseDto;
import com.qurve.vocabulary.enums.UnitStatus;
import com.qurve.vocabulary.repository.BookmarkRepository;
import com.qurve.vocabulary.repository.UnitProgressRepository;
import com.qurve.vocabulary.repository.UserWordStudyRepository;
import com.qurve.vocabulary.repository.VocabularyWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private static final Set<String> SUPPORTED_LEVELS = Set.of("N1", "N2", "N3", "N4", "N5");
    private static final Set<String> SUPPORTED_ENGLISH_LEVELS = Set.of("A1", "A2", "B1", "B2", "C1", "C2");

    private final UserRepository userRepository;
    private final UnitProgressRepository unitProgressRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ChallengeRepository challengeRepository;
    private final BadgeService badgeService;
    private final UserWordStudyRepository userWordStudyRepository;
    private final ChallengeProgressService challengeProgressService;
    private final XpService xpService;
    private final AttendanceService attendanceService;

    /**
     * 사용자의 현재 학습 언어에 해당하는 단어 유닛 목록을 조회한다.
     *
     * 단어 마스터 데이터를 기준으로 유닛 목록을 생성하고,
     * 사용자 학습 기록이 있는 유닛에는 저장된 진행 상태를 반영한다.
     * 학습 기록이 없는 유닛은 BEFORE 상태로 반환한다.
     *
     * @param loginId 로그인 ID
     * @param level 일본어 JLPT 레벨(N1~N5) 또는 영어 CEFR 레벨(A1~C2)
     * @return 레벨별 단어 유닛 목록과 사용자 학습 상태
     * @throws BusinessException 사용자가 없거나 레벨이 유효하지 않거나 유닛이 없는 경우
     */
    public List<UnitProgressResponseDto> getUnitList(String loginId, String level) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 학습 언어가 설정되지 않은 기존 사용자는 일본어로 처리한다.
        LearningLanguage language = user.getLearningLanguage() == null ? LearningLanguage.JAPANESE : user.getLearningLanguage();

        String normalizedLevel = normalizeLevel(level, language);

        // 선택한 학습 언어와 레벨에 해당하는 단어 유닛 조회
        List<Integer> unitNumbers = vocabularyWordRepository.findUnitNumbersByLanguageAndLevel(language, LearningLanguage.JAPANESE, normalizedLevel);

        if (unitNumbers.isEmpty()) {
            throw new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND);
        }

        // 같은 언어와 레벨의 사용자 학습 상태 조회
        Map<Integer, UnitStatus> savedStatusMap = unitProgressRepository.findProgressByLanguageAndLevel(user,language,LearningLanguage.JAPANESE,normalizedLevel)
                .stream()
                .collect(Collectors.toMap(
                        UnitProgress::getUnitNumber,
                        UnitProgress::getStatus,
                        (existing, replacement) -> existing
                ));

        return unitNumbers.stream()
                .map(unitNumber -> UnitProgressResponseDto.of(
                        normalizedLevel,
                        unitNumber,
                        savedStatusMap.getOrDefault(unitNumber, UnitStatus.BEFORE)
                ))
                .toList();
    }

    /**
     * JLPT 레벨 값 검증
     *
     * * 사용자가 소문자나 공백이 포함된 값으로 요청해도
     * DB에 저장된 레벨 형식과 비교할 수 있도록 N1~N5 형태로 변환한다.
     *
     * @param level 요청으로 전달된 JLPT 레벨
     * @return 검증이 완료된 JLPT 레벨
     * @throws BusinessException 지원하지 않는 레벨인 경우
     */
    private String normalizeLevel(String level) {
        String normalizedLevel = level == null ? "" : level.trim().toUpperCase(Locale.ROOT);

        if (!SUPPORTED_LEVELS.contains(normalizedLevel)) {
            throw new BusinessException(ErrorCode.INVALID_LEVEL);
        }

        return normalizedLevel;
    }

    /**
     * 학습 언어에 맞는 레벨인지 검증하고 앞뒤 공백 제거 및 대문자 변환을 수행한다.
     *
     * 일본어는 N1~N5, 영어는 A1~C2를 허용한다.
     *
     * @param level 요청 레벨
     * @param language 조회할 학습 언어
     * @return 정규화된 레벨
     * @throws BusinessException 해당 언어에서 지원하지 않는 레벨인 경우
     */
    private String normalizeLevel(String level, LearningLanguage language) {

        if (language == LearningLanguage.JAPANESE)
            return normalizeLevel(level);

        String normalizedLevel = level == null ? "" : level.trim().toUpperCase(Locale.ROOT);

        if (!SUPPORTED_ENGLISH_LEVELS.contains(normalizedLevel))
            throw new BusinessException(ErrorCode.INVALID_LEVEL);

        return normalizedLevel;
    }

    /**
     * 유닛 단어 학습 조회
     *
     * * 단어 학습 화면에서 사용할 단어 목록을 조회한다.
     *
     * * 단어는 레벨과 유닛 번호를 기준으로 조회하며,
     * 화면 표시 순서를 위해 조회된 순서대로 번호를 부여한다.
     *
     * @param loginId 로그인 ID
     * @param level 조회할 JLPT 레벨
     * @param unitNumber 조회할 유닛 번호
     * @return 유닛 단어 학습 정보
     * @throws BusinessException 유저, 레벨, 유닛 정보가 유효하지 않은 경우
     */
    public UnitWordStudyResponseDto getUnitWords(String loginId, String level, Integer unitNumber) {

        String normalizedLevel = normalizeLevel(level);

        userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (unitNumber == null || unitNumber < 1) {
            throw new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND);
        }

        List<VocabularyWord> words = vocabularyWordRepository
                .findByLevelAndUnitNumberOrderByWordIdAsc(normalizedLevel, unitNumber);

        if (words.isEmpty()) {
            throw new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND);
        }

        List<UnitWordResponseDto> wordResponses = java.util.stream.IntStream.range(0, words.size())
                .mapToObj(index -> UnitWordResponseDto.from(words.get(index), index + 1))
                .toList();

        return UnitWordStudyResponseDto.of(normalizedLevel, unitNumber, wordResponses);
    }

    /**
     * 단어 북마크 추가
     *
     * * 단어 학습 중 북마크 버튼 클릭 시 해당 단어를 북마크에 추가한다.
     * * 이미 북마크된 단어인 경우 예외를 발생시킨다.
     *
     * @param loginId 로그인 ID
     * @param wordId 북마크할 단어 ID
     * @throws BusinessException 유저가 존재하지 않거나 이미 북마크된 단어인 경우
     */
    @Transactional
    public void addBookmark(String loginId, Long wordId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        vocabularyWordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND));

        if (bookmarkRepository.existsByUserAndWordId(user, wordId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_BOOKMARK);
        }

        bookmarkRepository.save(Bookmark.builder()
                .user(user)
                .wordId(wordId)
                .createdAt(LocalDateTime.now())
                .build());

        xpService.grantXpOnce(user, XpActionType.WORD_BOOKMARK, wordId);

        badgeService.evaluate(user);
    }

    /**
     * 단어 북마크 삭제
     *
     * * 북마크된 단어를 북마크에서 제거한다.
     *
     * @param loginId 로그인 ID
     * @param wordId 북마크 삭제할 단어 ID
     * @throws BusinessException 유저가 존재하지 않거나 북마크가 존재하지 않는 경우
     */
    @Transactional
    public void removeBookmark(String loginId, Long wordId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findByUserAndWordId(user, wordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }

    /**
     * 유닛 학습 시작
     *
     * * 단어보기 클릭 시 해당 유닛의 학습 상태를 IN_PROGRESS로 변경한다.
     * * UnitProgress가 없는 경우 새로 생성 후 IN_PROGRESS로 저장한다.
     *
     * @param loginId 로그인 ID
     * @param level 조회할 JLPT 레벨
     * @param unitNumber 학습 시작할 유닛 번호
     * @throws BusinessException 유저가 존재하지 않거나 레벨이 유효하지 않은 경우
     */
    @Transactional
    public void startUnit(String loginId, String level, Integer unitNumber){

        String normalizedLevel = normalizeLevel(level);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UnitProgress unitProgress = unitProgressRepository.findByUserAndLevelAndUnitNumber(user, normalizedLevel, unitNumber)
                .orElse(UnitProgress.builder()
                        .user(user)
                        .level(normalizedLevel)
                        .unitNumber(unitNumber)
                        .status(UnitStatus.BEFORE)
                        .updatedAt(LocalDateTime.now())
                        .build());

        if (unitProgress.getStatus() != UnitStatus.COMPLETED) {
            unitProgress.updateStatus(UnitStatus.IN_PROGRESS);
            unitProgressRepository.save(unitProgress);
        }
    }

    /**
     * 유닛 학습 완료
     *
     * * 학습완료 버튼 클릭 시 해당 유닛의 학습 상태를 COMPLETED로 변경한다.
     * * UnitProgress가 없는 경우 새로 생성 후 COMPLETED로 저장한다.
     *
     * @param loginId 로그인 ID
     * @param level 조회할 JLPT 레벨
     * @param unitNumber 학습 완료할 유닛 번호
     * @throws BusinessException 유저가 존재하지 않거나 레벨이 유효하지 않은 경우
     */
    @Transactional
    public void completeUnit(String loginId, String level, Integer unitNumber){

        String normalizedLevel = normalizeLevel(level);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UnitProgress unitProgress = unitProgressRepository.findByUserAndLevelAndUnitNumber(user, normalizedLevel, unitNumber)
                .orElse(UnitProgress.builder()
                        .user(user)
                        .level(normalizedLevel)
                        .unitNumber(unitNumber)
                        .status(UnitStatus.BEFORE)
                        .updatedAt(LocalDateTime.now())
                        .build());

        boolean alreadyCompleted = unitProgress.getStatus() == UnitStatus.COMPLETED;

        unitProgress.updateStatus(UnitStatus.COMPLETED);
        unitProgressRepository.save(unitProgress);

        attendanceService.save(loginId);

        if (!alreadyCompleted)
            xpService.grantXp(user, XpActionType.WORD_SET_COMPLETE);

        List<VocabularyWord> words = vocabularyWordRepository
                .findByLevelAndUnitNumberOrderByWordIdAsc(normalizedLevel, unitNumber);

        if (words.isEmpty()) {
            throw new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND);
        }

        Set<Long> studiedWordIds = userWordStudyRepository.findAllByUserAndWordIn(user, words)
                .stream()
                .map(userWordStudy -> userWordStudy.getWord().getWordId())
                .collect(Collectors.toSet());

        List<UserWordStudy> newStudies = words.stream()
                .filter(word -> !studiedWordIds.contains(word.getWordId()))
                .map(word -> UserWordStudy.builder()
                        .user(user)
                        .word(word)
                        .build())
                .toList();

        if (newStudies.isEmpty()) {
            return;
        }

        userWordStudyRepository.saveAll(newStudies);
        challengeProgressService.addProgress(user, ChallengeGoalType.WORD_COUNT, newStudies.size());
        newStudies.forEach(ignored -> xpService.grantXp(user, XpActionType.WORD_LEARN));
    }

    /**
     * 챌린지 단어 조회
     *
     * 사용자의 현재 학습 언어에 해당하는 단어 중에서
     * 진행 중인 WORD_COUNT 챌린지의 목표 개수까지 무작위로 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 현재 학습 언어의 챌린지 단어 목록
     * @throws BusinessException 사용자가 없거나 진행 중인 WORD_COUNT 챌린지가 없는 경우
     */
    public List<UnitWordResponseDto> getChallengeWords(String loginId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Challenge challenge = findActiveWordChallenge(user);

        // 학습 언어가 없는 기존 사용자는 일본어로 처리한다.
        LearningLanguage language = user.getLearningLanguage() == null ? LearningLanguage.JAPANESE : user.getLearningLanguage();

        List<VocabularyWord> words = vocabularyWordRepository.findRandomByLanguage(language, LearningLanguage.JAPANESE, challenge.getTargetValue());

        return words.stream()
                .map(word -> UnitWordResponseDto.from(word, 0))
                .toList();
    }

    /**
     * 챌린지 단어 학습을 완료 처리합니다.
     *
     * * 이미 학습한 단어는 다시 저장하거나 챌린지 진행도에 반영하지 않아,
     * 완료 요청을 재전송해도 중복 적립되지 않습니다.
     *
     * @param loginId 로그인 ID
     * @param requestDto 완료한 단어 ID 목록
     * @return 요청 단어 수와 새로 학습 처리된 단어 수
     * @throws BusinessException 사용자가 없거나 단어/진행 중 단어 챌린지가 없는 경우
     */
    @Transactional
    public ChallengeWordCompleteResponseDto completeChallengeWords(
            String loginId,
            ChallengeWordCompleteRequestDto requestDto
    ) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        findActiveWordChallenge(user);

        List<Long> wordIds = new java.util.ArrayList<>(new LinkedHashSet<>(requestDto.getWordIds()));
        List<VocabularyWord> words = vocabularyWordRepository.findAllById(wordIds);

        if (words.size() != wordIds.size()) {
            throw new BusinessException(ErrorCode.VOCABULARY_WORD_NOT_FOUND);
        }

        Set<Long> studiedWordIds = userWordStudyRepository.findAllByUserAndWordIn(user, words)
                .stream()
                .map(userWordStudy -> userWordStudy.getWord().getWordId())
                .collect(Collectors.toSet());

        List<UserWordStudy> newStudies = words.stream()
                .filter(word -> !studiedWordIds.contains(word.getWordId()))
                .map(word -> UserWordStudy.builder()
                        .user(user)
                        .word(word)
                        .build())
                .toList();

        if (!newStudies.isEmpty()) {
            userWordStudyRepository.saveAll(newStudies);
            challengeProgressService.addProgress(user, ChallengeGoalType.WORD_COUNT, newStudies.size());
            newStudies.forEach(ignored -> xpService.grantXp(user, XpActionType.WORD_LEARN));
            badgeService.evaluate(user);
        }

        attendanceService.save(loginId);

        return ChallengeWordCompleteResponseDto.of(requestDto.getWordIds().size(), newStudies.size());
    }

    private Challenge findActiveWordChallenge(User user) {
        return challengeRepository.findFirstByUserAndGoalTypeAndStatusOrderByCreatedAtDesc(
                        user,
                        ChallengeGoalType.WORD_COUNT,
                        ChallengeStatus.ACTIVE
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
    }

    /**
     * 북마크 단어 조회
     *
     * * 사용자가 북마크한 단어 목록을 조회한다.
     * * 북마크 테이블의 wordId를 기반으로 단어 정보를 조회하여 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 북마크 단어 목록
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public List<UnitWordResponseDto> getBookmarks(String loginId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);

        // 북마크된 단어 ID 목록 추출
        List<Long> wordIds = bookmarks.stream()
                .map(Bookmark::getWordId)
                .toList();

        List<VocabularyWord> words = vocabularyWordRepository.findAllById(wordIds);

        // 순서 번호(1부터) 부여하여 반환
        return IntStream.range(0, words.size())
                .mapToObj(i -> UnitWordResponseDto.from(words.get(i), i + 1))
                .toList();
    }
}
