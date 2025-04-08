package com.reverie.reverie.service;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.repo.JournalRepo;
import com.reverie.reverie.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {
    private final JournalRepo journalRepo;
    private final UserRepo userRepo;

    @Autowired
    public JournalService(JournalRepo journalRepo, UserRepo userRepo) {
        this.journalRepo = journalRepo;
        this.userRepo = userRepo;
    }

    public Journal createJournal(String userId, Journal journal) {
        return userRepo.findById(userId).map(user -> {
            // Check if journal for the same date (title) already exists for this user
            Journal existingJournal = journalRepo.findByUserAndTitle(user, journal.getTitle());

            if (existingJournal != null) {
                // Update the existing journal's content and timestamp
                existingJournal.setContent(journal.getContent());
                existingJournal.setCreatedAt(journal.getCreatedAt());
                return journalRepo.save(existingJournal);
            }

            // Else, create new journal
            journal.setUser(user);
            return journalRepo.save(journal);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }


    public List<Journal> getUserJournals(String userId) {
        return journalRepo.findByUserId(userId);
    }

    public List<Journal> searchJournal(String userId, String keyword) {
        return journalRepo.searchJournal(userId, keyword);
    }

    public Journal updateJournal(Long id, Journal updatedJournal) {
        return journalRepo.findById(id)
                .map(existingJournal -> {
                    existingJournal.setTitle(updatedJournal.getTitle());
                    existingJournal.setContent(updatedJournal.getContent());
                    return journalRepo.save(existingJournal);
                }).orElseThrow(() -> new RuntimeException("Journal not found " + id));
    }

    public void deleteJournal(Long id) {
        if (!journalRepo.existsById(id)) {
            throw new RuntimeException("Journal not found " + id);
        }
        journalRepo.deleteById(id);
    }
}
