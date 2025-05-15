package com.reverie.reverie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverie.reverie.model.*;
import com.reverie.reverie.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class JournalService {
    private final JournalRepo journalRepo;
    private final UserRepo userRepo;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final SentimentsRepo sentimentsRepo;
    private final EmotionsRepo emotionsRepo;
    private final KeywordsRepo keywordsRepo;
    private final JournalKeywordsRepo journalKeywordsRepo;
    private final ObjectMapper objectMapper;

    @Autowired
    public JournalService(JournalRepo journalRepo,
            UserRepo userRepo,
            SentimentAnalysisService sentimentAnalysisService,
            SentimentsRepo sentimentsRepo,
            EmotionsRepo emotionsRepo,
            KeywordsRepo keywordsRepo,
            JournalKeywordsRepo journalKeywordsRepo,
            ObjectMapper objectMapper) {
        this.journalRepo = journalRepo;
        this.userRepo = userRepo;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.sentimentsRepo = sentimentsRepo;
        this.emotionsRepo = emotionsRepo;
        this.keywordsRepo = keywordsRepo;
        this.journalKeywordsRepo = journalKeywordsRepo;
        this.objectMapper = objectMapper;
    }

    public Journal getJournalByUserAndDateRange(String userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return userRepo.findById(userId)
                .map(user -> journalRepo.findByUserAndCreatedAtBetween(user, startOfDay, endOfDay))
                .orElse(null);
    }

    public Journal getJournalByUserAndDate(String userId, LocalDateTime date) {
        return userRepo.findById(userId)
                .map(user -> journalRepo.findByUserAndCreatedAt(user, date))
                .orElse(null);
    }

    public Map<String, Object> getAnalyticsForJournal(Journal journal) {
        Map<String, Object> analyticsData = new HashMap<>();

        Sentiments sentiment = sentimentsRepo.findByJournal(journal).orElse(null);
        sentiment.setJournal(null);
        List<Emotions> emotions = emotionsRepo.findByJournal(journal);
        emotions.forEach(emotion -> emotion.setJournal(null));
        List<String> keywords = journalKeywordsRepo.findByJournal(journal)
                .stream()
                .map(jk -> jk.getKeyword().getWord())
                .collect(Collectors.toList());

        analyticsData.put("sentiment", sentiment);
        analyticsData.put("emotions", emotions);
        analyticsData.put("keywords", keywords);

        return analyticsData;
    }

    public Journal createJournal(String userId, Journal journal) {
        return userRepo.findById(userId).map(user -> {
            journal.setUser(user);
            Journal saved = journalRepo.save(journal);

            SentimentAnalysisService.TextAnalysis analysis = sentimentAnalysisService
                    .analyzeSentiment(journal.getContent());

            System.out.println(analysis);

            // Save sentiment
            Sentiments sentiment = new Sentiments();
            sentiment.setJournal(saved);
            sentiment.setLabel(analysis.getSentiment().getLabel());
            sentiment.setScore(analysis.getSentiment().getScore());
            sentimentsRepo.save(sentiment);

            // Save emotions
            for (SentimentAnalysisService.SentimentEmotionResult emo : analysis.getEmotions()) {
                Emotions emotion = new Emotions();
                emotion.setJournal(saved);
                emotion.setLabel(emo.getLabel());
                emotion.setScore(emo.getScore());
                emotionsRepo.save(emotion);
            }

            // Save keywords
            for (String keyword : analysis.getKeywords()) {
                Keywords keywordEntity = keywordsRepo.findByWord(keyword)
                        .orElseGet(() -> keywordsRepo.save(new Keywords(null, keyword)));

                Journal_keywords link = new Journal_keywords(saved, keywordEntity);
                journalKeywordsRepo.save(link);
            }

            return saved;
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public Journal updateJournal(Long id, Journal updatedJournal) {
        return journalRepo.findById(id).map(existingJournal -> {
            existingJournal.setTitle(updatedJournal.getTitle());
            existingJournal.setContent(updatedJournal.getContent());
            existingJournal.setCreatedAt(updatedJournal.getCreatedAt());
            Journal saved = journalRepo.save(existingJournal);

            // Clear old data
            sentimentsRepo.deleteByJournal(existingJournal);
            emotionsRepo.deleteByJournal(existingJournal);
            journalKeywordsRepo.deleteByJournal(existingJournal);

            // Re-analyze
            SentimentAnalysisService.TextAnalysis analysis = sentimentAnalysisService
                    .analyzeSentiment(saved.getContent());

            // Save sentiment
            Sentiments sentiment = new Sentiments();
            sentiment.setJournal(saved);
            sentiment.setLabel(analysis.getSentiment().getLabel());
            sentiment.setScore(analysis.getSentiment().getScore());
            sentimentsRepo.save(sentiment);

            // Save emotions
            for (SentimentAnalysisService.SentimentEmotionResult emo : analysis.getEmotions()) {
                Emotions emotion = new Emotions();
                emotion.setJournal(saved);
                emotion.setLabel(emo.getLabel());
                emotion.setScore(emo.getScore());
                emotionsRepo.save(emotion);
            }

            // Save keywords
            for (String keyword : analysis.getKeywords()) {
                Keywords keywordEntity = keywordsRepo.findByWord(keyword)
                        .orElseGet(() -> keywordsRepo.save(new Keywords(null, keyword)));

                Journal_keywords link = new Journal_keywords(saved, keywordEntity);
                journalKeywordsRepo.save(link);
            }

            return saved;
        }).orElseThrow(() -> new RuntimeException("Journal not found " + id));
    }

    public List<Journal> getUserJournals(String userId) {
        return journalRepo.findByUserId(userId);
    }

    public List<Journal> searchJournal(String userId, String keyword) {
        return journalRepo.searchJournal(userId, keyword);
    }

    public void deleteJournal(Long id) {
        if (!journalRepo.existsById(id)) {
            throw new RuntimeException("Journal not found " + id);
        }

        // Delete all related data
        Journal journal = journalRepo.findById(id).orElseThrow();
        sentimentsRepo.deleteByJournal(journal);
        emotionsRepo.deleteByJournal(journal);
        journalKeywordsRepo.deleteByJournal(journal);

        journalRepo.deleteById(id);
    }
}