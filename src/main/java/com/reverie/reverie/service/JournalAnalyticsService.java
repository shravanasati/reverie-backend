package com.reverie.reverie.service;

import com.reverie.reverie.model.Emotions;
import com.reverie.reverie.model.Journal;
import com.reverie.reverie.model.Sentiments;
import com.reverie.reverie.repo.EmotionsRepo;
import com.reverie.reverie.repo.JournalRepo;
import com.reverie.reverie.repo.SentimentsRepo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JournalAnalyticsService {
	private final JournalRepo journalRepo;
	private final EmotionsRepo emotionsRepo;
	private final SentimentsRepo sentimentsRepo;

	@Autowired
	public JournalAnalyticsService(JournalRepo journalRepo,
								   EmotionsRepo emotionsRepo,
								   SentimentsRepo sentimentsRepo) {
		this.journalRepo = journalRepo;
		this.emotionsRepo = emotionsRepo;
		this.sentimentsRepo = sentimentsRepo;
	}

	public Map<String, Object> getJournalAnalytics(String userId) {
		List<Journal> journals = journalRepo.findByUserIdOrderByCreatedAtDesc(userId);
		TreeSet<LocalDateTime> journalDates = journals.stream()
				.map(j -> j.getCreatedAt().truncatedTo(ChronoUnit.DAYS))
				.collect(Collectors.toCollection(TreeSet::new));

		List<Emotions> emotions = emotionsRepo.findByJournalUserId(userId);
		List<Sentiments> sentiments = sentimentsRepo.findByJournalUserId(userId);

		Map<String, Object> analytics = new HashMap<>();
		analytics.put("totalJournals", journals.size());
		analytics.put("currentStreak", calculateCurrentStreak(journalDates));
		analytics.put("longestStreak", calculateLongestStreak(journalDates));
		analytics.put("emotions", emotions);
		analytics.put("sentiments", sentiments);

		return analytics;
	}

	private int calculateCurrentStreak(TreeSet<LocalDateTime> dates) {
		if (dates.isEmpty())
			return 0;

		LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
		int streak = 0;

		for (LocalDateTime date = today; dates.contains(date); date = date.minusDays(1)) {
			streak++;
		}

		return streak;
	}

	private int calculateLongestStreak(TreeSet<LocalDateTime> dates) {
		if (dates.isEmpty())
			return 0;

		int maxStreak = 1;
		int currentStreak = 1;

		Iterator<LocalDateTime> iter = dates.iterator();
		LocalDateTime prev = iter.next();

		while (iter.hasNext()) {
			LocalDateTime curr = iter.next();
			if (ChronoUnit.DAYS.between(prev, curr) == 1) {
				currentStreak++;
				maxStreak = Math.max(maxStreak, currentStreak);
			} else {
				currentStreak = 1;
			}
			prev = curr;
		}

		return maxStreak;
	}

	@Data
	public static class JournalStats {
		private final int totalJournals;
		private final int currentStreak;
		private final int longestStreak;
	}
}
