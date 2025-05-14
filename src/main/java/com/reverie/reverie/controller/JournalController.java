package com.reverie.reverie.controller;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/journals")
@CrossOrigin
public class JournalController {

    private final JournalService journalService;

    @Autowired
    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    // Create journal if it doesn't exist for the date
    @PostMapping("/{userId}")
    public ResponseEntity<?> createJournal(
            @PathVariable String userId,
            @RequestBody Journal journal) {
        journal.setId(null);
        try {
            LocalDateTime dateTime = journal.getCreatedAt();
            if (dateTime == null) {
                return ResponseEntity.badRequest().body("Created date is required");
            }

            dateTime = dateTime.toLocalDate().atStartOfDay();
            journal.setCreatedAt(dateTime);

            Journal existing = journalService.getJournalByUserAndDateRange(
                    userId,
                    dateTime,
                    dateTime.plusDays(1));

            if (existing != null) {
                Journal updatedJournal = journalService.updateJournal(existing.getId(), journal);
                return new ResponseEntity<>(updatedJournal, HttpStatus.OK);
            }

            Journal created = journalService.createJournal(userId, journal);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Explicitly update journal
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJournal(@PathVariable Long id, @RequestBody Journal journal) {
        try {
            Journal updatedJournal = journalService.updateJournal(id, journal);
            return new ResponseEntity<>(updatedJournal, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get journal by user and date
    @GetMapping("/{userId}/{date}")
    public ResponseEntity<?> getJournalByDate(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false, defaultValue = "false") boolean analytics) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            Journal journal = journalService.getJournalByUserAndDateRange(userId, startOfDay, endOfDay);
            if (journal != null) {
                Map<String, Object> response = new HashMap<>();
                journal.setUser(null);
                response.put("journal", journal);
                if (analytics) {
                    Map<String, Object> analyticsData = journalService.getAnalyticsForJournal(journal);
                    response.put("analytics", analyticsData);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all journals by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Journal>> getUserJournals(@PathVariable String userId) {
        return new ResponseEntity<>(journalService.getUserJournals(userId), HttpStatus.OK);
    }

    // Search journals by keyword
    @GetMapping("/search")
    public ResponseEntity<List<Journal>> searchJournal(@RequestParam String userId, @RequestParam String keyword) {
        List<Journal> journal = journalService.searchJournal(userId, keyword);
        return new ResponseEntity<>(journal, HttpStatus.OK);
    }

    // Delete journal
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJournal(@PathVariable Long id) {
        try {
            journalService.deleteJournal(id);
            return new ResponseEntity<>("Deleted!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
