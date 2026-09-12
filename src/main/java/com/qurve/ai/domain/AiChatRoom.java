package com.qurve.ai.domain;

import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_ai_chat_room", uniqueConstraints = {@UniqueConstraint(name = "uk_ai_chat_room_user_language", columnNames = {"user_id", "learning_language"})})
public class AiChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_language", nullable = false, length = 20)
    private LearningLanguage learningLanguage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}