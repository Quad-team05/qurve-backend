package com.qurve.vocabulary.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.repository.UnitProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private final UserRepository userRepository;
    private final UnitProgressRepository unitProgressRepository;

    public List<UnitProgressResponseDto> getUnitList(String loginId, String level) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UnitProgress> unitList = unitProgressRepository.findByUserAndLevel(user, level);

        return unitList.stream()
                .map(UnitProgressResponseDto::from)
                .collect(Collectors.toList());
    }
}
