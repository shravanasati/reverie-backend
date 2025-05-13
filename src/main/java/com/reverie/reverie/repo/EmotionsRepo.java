package com.reverie.reverie.repo;

import com.reverie.reverie.model.Emotions;
import com.reverie.reverie.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionsRepo extends JpaRepository<Emotions, Long> {
    List<Emotions> findByJournal(Journal journal);
    void deleteByJournal(Journal journal);

    List<Emotions> findByJournalUserId(String userId);
}
