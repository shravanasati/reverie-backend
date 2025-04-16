package com.reverie.reverie.controller;

import com.reverie.reverie.service.JournalAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
	public ResponseEntity<?> getJournalAnalytics(@PathVariable String userId) {
		try {
			Map<String, Object> analytics = analyticsService.getJournalAnalytics(userId);
			return ResponseEntity.ok(analytics);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
