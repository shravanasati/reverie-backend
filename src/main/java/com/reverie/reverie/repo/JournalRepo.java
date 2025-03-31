package com.reverie.reverie.repo;

import com.reverie.reverie.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JournalRepo extends JpaRepository<Journal, Long> {
    List<Journal> findByUserId(Long userId);
    @Query("SELECT j FROM Journal j WHERE " +
            "j.user.id = :userId AND LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Journal> searchJournal(@Param("userId") Long userId,@Param("keyword") String keyword);
    //also search from content?
}
