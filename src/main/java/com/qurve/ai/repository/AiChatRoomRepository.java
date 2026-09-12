package com.qurve.ai.repository;

import com.qurve.ai.domain.AiChatRoom;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiChatRoomRepository extends JpaRepository<AiChatRoom, Long> {

    Optional<AiChatRoom> findByUserAndLearningLanguage(User user, LearningLanguage learningLanguage);
    boolean existsByUser(User user);
}