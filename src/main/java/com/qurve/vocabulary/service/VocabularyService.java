package com.qurve.vocabulary.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.enums.UnitStatus;
import com.qurve.vocabulary.repository.UnitProgressRepository;
import com.qurve.vocabulary.repository.VocabularyWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private String normalizeLevel(String level) {
        String normalizedLevel = level == null ? "" : level.trim().toUpperCase(Locale.ROOT);

        if (!SUPPORTED_LEVELS.contains(normalizedLevel)) {
            throw new BusinessException(ErrorCode.INVALID_LEVEL);
        }

        return normalizedLevel;
    }
}