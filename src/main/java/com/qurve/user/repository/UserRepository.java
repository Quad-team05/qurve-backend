package com.qurve.user.repository;

import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByLoginIdAndIsDeletedFalse(String loginId);
    boolean existsByEmail(String email);
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);
    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByNameAndEmailAndIsDeletedFalse(String name, String email);
    Optional<User> findByLoginIdAndEmail(String loginId, String email);
    Optional<User> findByLoginIdAndEmailAndIsDeletedFalse(String loginId, String email);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Optional<User> findByGoogleSub(String googleSub);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select u
        from User u
        where u.loginId = :loginId
          and u.isDeleted = false
        """)
    Optional<User> findForGoogleLink(@Param("loginId") String loginId);
}
