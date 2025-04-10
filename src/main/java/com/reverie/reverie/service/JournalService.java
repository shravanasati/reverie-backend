package com.reverie.reverie.service;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.repo.JournalRepo;
import com.reverie.reverie.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.reverie.reverie.service.SentimentAnalysisService;


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
    public Journal getJournalByUserAndDate(String userId, LocalDateTime date) {
        return userRepo.findById(userId).map(user ->
                journalRepo.findByUserAndCreatedAtBetween(
                        user,
                        date,
                        date.plusDays(1).minusNanos(1)
                )
        ).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    public Journal createJournal(String userId, Journal journal) {
        return userRepo.findById(userId).map(user -> {

            LocalDateTime journalDate = journal.getCreatedAt().toLocalDate().atStartOfDay();
            Journal existingJournal = journalRepo.findByUserAndCreatedAtBetween(
                    user,
                    journalDate,
                    journalDate.plusDays(1).minusNanos(1)
            );

            if (existingJournal != null) {
                existingJournal.setTitle(journal.getTitle());
                existingJournal.setContent(journal.getContent());
                existingJournal.setCreatedAt(journal.getCreatedAt());
                SentimentAnalysisService.SentimentAnalysis analysis =
                        sentimentAnalysisService.analyzeSentiment(existingJournal.getContent());
                existingJournal.setEmotion(analysis.getEmotion());
                existingJournal.setSentimentScore(analysis.getScore());
                return journalRepo.save(existingJournal);
            }

            // Create new journal if none exists for that day
            journal.setUser(user);
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
