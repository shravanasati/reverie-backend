package com.reverie.reverie.repo;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalRepo extends JpaRepository<Journal, Long> {
    List<Journal> findByUserId(String userId);

    @Query("SELECT j FROM Journal j WHERE " +
            "j.user.id = :userId AND LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Journal> searchJournal(@Param("userId") String userId, @Param("keyword") String keyword);

    Journal findByUserAndTitle(User user, String title);
}
