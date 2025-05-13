package com.reverie.reverie.repo;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.model.JournalKeywordId;
import com.reverie.reverie.model.Journal_keywords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalKeywordsRepo extends JpaRepository<Journal_keywords, JournalKeywordId> {
    List<Journal_keywords> findByJournal(Journal journal);
    void deleteByJournal(Journal journal);
}
