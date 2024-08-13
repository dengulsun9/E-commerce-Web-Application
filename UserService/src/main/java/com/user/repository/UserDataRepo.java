package com.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.entity.UserData;

public interface UserDataRepo extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUsernameAndPassword(String username, String password);
}
