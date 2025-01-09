package com.mysite.sbb.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByusername(String username);

    // 이메일로 비밀번호 업데이트하는 쿼리
    @Transactional
    @Modifying
    @Query("UPDATE SiteUser u SET u.password = :newPassword WHERE u.email = :email")
    void updatePasswordByEmail(String email, String newPassword);

    // 현재 비밀번호로 사용자 조회
    Optional<SiteUser> findByPassword(String password);

    // 이메일로 사용자 조회
    Optional<SiteUser> findByEmail(String email);
}