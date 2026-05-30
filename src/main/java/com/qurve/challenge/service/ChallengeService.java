package com.qurve.challenge.service;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeGoalTypeResponseDto;
import com.qurve.challenge.dto.response.ChallengeManageResponseDto;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public List<ChallengeGoalTypeResponseDto> getGoalTypes() {
        return Arrays.stream(ChallengeGoalType.values())
                .map(ChallengeGoalTypeResponseDto::from)
                .toList();
    }

    public List<ChallengeManageResponseDto> getMyChallenges(String loginId) {
        return challengeRepository.findAllByUser_LoginId(loginId)
                .stream()
                .map(ChallengeManageResponseDto::from)
                .toList();
    }

    @Transactional
    public ChallengeCreateResponseDto createChallenge(
            ChallengeCreateRequestDto requestDto,
            String loginId
    ) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Challenge challenge = requestDto.toEntity(user);
        Challenge savedChallenge = challengeRepository.save(challenge);

        return ChallengeCreateResponseDto.from(savedChallenge);
    }
}