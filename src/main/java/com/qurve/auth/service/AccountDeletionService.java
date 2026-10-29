package com.qurve.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Removes an account and all first-party records linked to it. */
@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final JdbcTemplate jdbcTemplate;

    public void deleteAccountData(Long userId) {
        // Delete dependent rows before the account to satisfy foreign-key constraints.
        jdbcTemplate.update("""
                delete m from tb_ai_chat_message m
                join tb_ai_chat_room r on m.room_id = r.room_id
                where r.user_id = ?
                """, userId);
        jdbcTemplate.update("delete from tb_ai_chat_room where user_id = ?", userId);

        jdbcTemplate.update("""
                delete cp from tb_challenge_progress cp
                join tb_challenge c on cp.challenge_id = c.challenge_id
                where c.user_id = ?
                """, userId);
        jdbcTemplate.update("delete from tb_challenge where user_id = ?", userId);

        jdbcTemplate.update("delete from tb_wrong_note_review where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_wrong_note where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_problem_bookmark where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_problem_submission where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_problem_set_completion where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_bookmark where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_user_word_study where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_unit_progress where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_attendance_record where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_daily_study_log where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_study_time_record where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_study_statistics where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_user_badge where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_xp_history where user_id = ?", userId);
        jdbcTemplate.update("delete from tb_user where user_id = ?", userId);
    }
}
