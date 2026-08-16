package com.qurve.ai.repository;

import com.qurve.ai.domain.AiChatMessage;
import com.qurve.ai.domain.AiChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
    List<AiChatMessage> findTop20ByRoomOrderByCreatedAtDesc(AiChatRoom room);
    void deleteAllByRoom(AiChatRoom room);
}