package com.reverie.reverie.repo;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.model.Sentiments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface SentimentsRepo  extends JpaRepository<Sentiments, Long> {
    Optional<Sentiments> findByJournal(Journal journal);
    void deleteByJournal(Journal journal);

    List<Sentiments> findByJournalUserId(String userId);
}
