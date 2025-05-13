package com.reverie.reverie.repo;

import com.reverie.reverie.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
    @NotNull Optional<? extends User> findByEmail(String email);
}
