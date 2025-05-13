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

    public User findOrCreateGoogleUser(String id, String email, String name, String picture) {
        return userRepo.findById(id)
                .map(existingUser -> {
                    existingUser.setEmail(email);
                    existingUser.setName(name);
                    existingUser.setImage(picture);
                    existingUser.setEmailVerified(true);
                    existingUser.setUpdatedAt(Instant.now());
                    return userRepo.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(id);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setImage(picture);
                    newUser.setEmailVerified(true);
                    newUser.setCreatedAt(Instant.now());
                    newUser.setUpdatedAt(Instant.now());
                    return userRepo.save(newUser);
                });
    }

    public User signinUserOrUpdate(User user) {
        // Try by ID first
        return userRepo.findById(user.getId())
                .or(() -> userRepo.findByEmail(user.getEmail()))
                .map(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setImage(user.getImage());
                    existingUser.setEmailVerified(true);
                    existingUser.setUpdatedAt(Instant.now());
                    return userRepo.save(existingUser);
                })
                .orElseGet(() -> {
                    user.setEmailVerified(true);
                    user.setCreatedAt(Instant.now());
                    user.setUpdatedAt(Instant.now());
                    return userRepo.save(user);
                });
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
