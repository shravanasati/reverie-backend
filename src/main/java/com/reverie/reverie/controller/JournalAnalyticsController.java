package com.reverie.reverie.controller;

import com.reverie.reverie.service.JournalAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal-analytics")
@CrossOrigin
public class JournalAnalyticsController {

	private final JournalAnalyticsService analyticsService;

	@Autowired
	public JournalAnalyticsController(JournalAnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/{userId}")
	public ResponseEntity<?> getJournalStats(@PathVariable String userId) {
		try {
			JournalAnalyticsService.JournalStats stats = analyticsService.getJournalStats(userId);
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
