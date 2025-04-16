package com.reverie.reverie.controller;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.service.JournalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/journals")  // Changed base path to be more specific
@CrossOrigin
public class JournalController {
    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> createJournal(
            @PathVariable String userId,
            @RequestBody Journal journal) {
        try {
            // Convert to LocalDateTime at start of day
            LocalDateTime dateTime = LocalDate.parse(journal.getCreatedAt().toString().split("T")[0])
                    .atStartOfDay();
            journal.setCreatedAt(dateTime);

            // Check for existing journal
            Journal existingJournal = journalService.getJournalByUserAndDateRange(
                    userId,
                    dateTime,
                    dateTime.plusDays(1)
            );

            if (existingJournal != null) {
                return updateJournal(existingJournal.getId(), journal);
            }

            Journal createdJournal = journalService.createJournal(userId, journal);
            return new ResponseEntity<>(createdJournal, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/{date}")
    public ResponseEntity<?> getJournalByDate(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            Journal journal = journalService.getJournalByUserAndDateRange(userId, startOfDay, endOfDay);
            if (journal != null) {
                return ResponseEntity.ok(journal);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/user/{userId}")  // Updated user journals endpoint
    public ResponseEntity<List<Journal>> getUserJournals(@PathVariable String userId) {
        return new ResponseEntity<>(journalService.getUserJournals(userId), HttpStatus.OK);
    }

    @GetMapping("/search")  // Simplified search endpoint
    public ResponseEntity<List<Journal>> searchJournal(@RequestParam String userId, @RequestParam String keyword) {
        List<Journal> journal = journalService.searchJournal(userId, keyword);
        System.out.println(keyword);
        return new ResponseEntity<>(journal, HttpStatus.OK);
    }

    @PutMapping("/{id}")  // Simplified update endpoint
    public ResponseEntity<?> updateJournal(@PathVariable Long id, @RequestBody Journal journal) {
        try {
            Journal updatedJournal = journalService.updateJournal(id, journal);
            return new ResponseEntity<>(updatedJournal, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")  // Simplified delete endpoint
    public ResponseEntity<?> deleteJournal(@PathVariable Long id) {
        try {
            journalService.deleteJournal(id);
            return new ResponseEntity<>("Deleted!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
