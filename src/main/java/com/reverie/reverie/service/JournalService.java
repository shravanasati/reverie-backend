package com.reverie.reverie.service;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.repo.JournalRepo;
import com.reverie.reverie.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import com.reverie.reverie.service.SentimentAnalysisService;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalService {
    private final JournalRepo journalRepo;
    private final UserRepo userRepo;
    private final SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    public JournalService(JournalRepo journalRepo, UserRepo userRepo,SentimentAnalysisService sentimentAnalysisService) {
        this.journalRepo = journalRepo;
        this.userRepo = userRepo;
        this.sentimentAnalysisService = sentimentAnalysisService;
    }
    public Journal getJournalByUserAndDateRange(String userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return userRepo.findById(userId)
                .map(user -> journalRepo.findByUserAndCreatedAtBetween(user, startOfDay, endOfDay))
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }


    public Journal createJournal(String userId, Journal journal) {
        return userRepo.findById(userId).map(user -> {
            journal.setUser(user);

            // Perform sentiment analysis
            SentimentAnalysisService.SentimentAnalysis analysis =
                    sentimentAnalysisService.analyzeSentiment(journal.getContent());
            journal.setEmotion(analysis.getEmotion());
            journal.setSentimentScore(analysis.getScore());

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
                    existingJournal.setCreatedAt(updatedJournal.getCreatedAt());

                    // Perform sentiment analysis
                    SentimentAnalysisService.SentimentAnalysis analysis =
                            sentimentAnalysisService.analyzeSentiment(existingJournal.getContent());
                    existingJournal.setEmotion(analysis.getEmotion());
                    existingJournal.setSentimentScore(analysis.getScore());

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
