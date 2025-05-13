package com.reverie.reverie.repo;

import com.reverie.reverie.model.Keywords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeywordsRepo extends JpaRepository<Keywords, Long> {
    Optional<Keywords> findByWord(String word);
}
