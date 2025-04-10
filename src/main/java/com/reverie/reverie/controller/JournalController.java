package com.reverie.reverie.controller;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.service.JournalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/journals")  // Changed base path to be more specific
@CrossOrigin
public class JournalController {
    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping("/{userId}")  // Simplified create endpoint
    public ResponseEntity<?> createJournal(@PathVariable String userId, @RequestBody Journal journal) {
        try {
            Journal createdJournal = journalService.createJournal(userId, journal);
            return new ResponseEntity<>(createdJournal, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/today/{userId}")
    public ResponseEntity<?> getTodaysJournal(@PathVariable String userId) {
        try {
            LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
            Journal journal = journalService.getJournalByUserAndDate(userId, today);
            if (journal != null) {
                return new ResponseEntity<>(journal, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
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
