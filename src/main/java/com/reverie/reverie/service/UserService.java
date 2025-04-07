package com.reverie.reverie.service;

import com.reverie.reverie.model.User;
import com.reverie.reverie.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    public User signinUserOrUpdate(User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepo.save(user);
    }

    public User updateUser(String id, User updatedUser) {
        return userRepo.findById(String.valueOf(id))
                .map(existingUser -> {
                    existingUser.setName(updatedUser.getName());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setEmailVerified(updatedUser.isEmailVerified());
                    existingUser.setImage(updatedUser.getImage());
                    existingUser.setUpdatedAt(Instant.now());
                    return userRepo.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(String id) {
        String userId = String.valueOf(id);
        if (!userRepo.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepo.deleteById(userId);
    }

    public User getUserById(String id) {
        return userRepo.findById(String.valueOf(id))
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
