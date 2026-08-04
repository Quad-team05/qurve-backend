package com.qurve.vocabulary.service;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.vocabulary.domain.Bookmark;
import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.vocabulary.domain.VocabularyWord;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordStudyResponseDto;
import com.qurve.vocabulary.enums.UnitStatus;
import com.qurve.vocabulary.repository.BookmarkRepository;
import com.qurve.vocabulary.repository.UnitProgressRepository;
import com.qurve.vocabulary.repository.VocabularyWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private static final Set<String> SUPPORTED_LEVELS = Set.of("N1", "N2", "N3", "N4", "N5");

    private final UserRepository userRepository;
    private final UnitProgressRepository unitProgressRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ChallengeRepository challengeRepository;

    /**
     * 단어 유닛 목록 조회
     *
     * * 단어장 목록은 사용자 학습 기록이 아니라 단어 마스터 데이터를 기준으로 생성한다.
     *
     * * 사용자별 학습 기록이 있는 경우에만 해당 유닛의 진행 상태를 반영한다.
     *
     * @param loginId 로그인 ID
     * @param level 조회할 JLPT 레벨
     * @return 단어 유닛 목록
     * @throws BusinessException 유저, 레벨, 단어 유닛 정보가 유효하지 않은 경우
     */
    public List<UnitProgressResponseDto> getUnitList(String loginId, String level) {

        String normalizedLevel = normalizeLevel(level);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 신규 사용자도 단어장을 볼 수 있도록 단어 마스터 테이블 기준으로 유닛 조회
        List<Integer> unitNumbers = vocabularyWordRepository.findDistinctUnitNumbersByLevel(normalizedLevel);

        if (unitNumbers.isEmpty()) {
            throw new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND);
        }

        // 사용자 학습 기록이 있는 유닛만 진행 상태 반영
        Map<Integer, UnitStatus> savedStatusMap = unitProgressRepository
                .findByUserAndLevelOrderByUnitNumberAsc(user, normalizedLevel)
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

        unitProgress.updateStatus(UnitStatus.IN_PROGRESS);
        unitProgressRepository.save(unitProgress);
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

        unitProgress.updateStatus(UnitStatus.COMPLETED);
        unitProgressRepository.save(unitProgress);
    }

    /**
     * 챌린지 단어 조회
     *
     * * 챌린지 목표 유형이 단어 암기(WORD_COUNT)인 챌린지의 목표 단어 개수만큼
     * 전체 단어 데이터셋에서 랜덤으로 단어를 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 챌린지 단어 목록
     * @throws BusinessException 유저가 존재하지 않거나 WORD_COUNT 챌린지가 없는 경우
     */
    public List<UnitWordResponseDto> getChallengeWords(String loginId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Challenge challenge = challengeRepository.findByUserAndGoalType(user, ChallengeGoalType.WORD_COUNT)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        List<VocabularyWord> words = vocabularyWordRepository.findRandom(challenge.getTargetValue());

        return words.stream()
                .map(word -> UnitWordResponseDto.from(word, 0))
                .toList();
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

        return bookmarks.stream()
                .map(bookmark -> {VocabularyWord word = vocabularyWordRepository.findById(bookmark.getWordId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_UNIT_NOT_FOUND));
                return UnitWordResponseDto.from(word, 0);
                })
                .toList();
    }
}