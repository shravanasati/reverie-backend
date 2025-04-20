package com.reverie.reverie.service;

import com.reverie.reverie.model.Journal;
import com.reverie.reverie.repo.JournalRepo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class JournalAnalyticsService {

	private final JournalRepo journalRepo;

	@Autowired
	public JournalAnalyticsService(JournalRepo journalRepo) {
		this.journalRepo = journalRepo;
	}

	public Map<String, Object> getJournalAnalytics(String userId) {
		List<Journal> journals = journalRepo.findByUserIdOrderByCreatedAtDesc(userId);
		TreeSet<LocalDateTime> journalDates = journals.stream()
				.map(j -> j.getCreatedAt().truncatedTo(ChronoUnit.DAYS))
				.collect(Collectors.toCollection(TreeSet::new));

		Map<String, Object> analytics = new HashMap<>();
		analytics.put("totalJournals", journals.size());
		analytics.put("currentStreak", calculateCurrentStreak(journalDates));
		analytics.put("longestStreak", calculateLongestStreak(journalDates));
		analytics.put("emotions", journals.stream().map(Journal::getEmotion).filter(Objects::nonNull).collect(Collectors.toList()));
		analytics.put("sentiments", journals.stream().map(Journal::getSentimentScore).filter(Objects::nonNull).collect(Collectors.toList()));

		return analytics;
	}

	@Data
	public static class JournalStats {
		private final int totalJournals;
		private final int currentStreak;
		private final int longestStreak;
	}

	public JournalStats getJournalStats(String userId) {
		List<Journal> journals = journalRepo.findByUserIdOrderByCreatedAtDesc(userId);

		int totalJournals = journals.size();
		if (totalJournals == 0) {
			return new JournalStats(0, 0, 0);
		}

		// Get unique dates from journals
		TreeSet<LocalDateTime> journalDates = journals.stream()
				.map(j -> j.getCreatedAt().truncatedTo(ChronoUnit.DAYS))
				.collect(Collectors.toCollection(TreeSet::new));

		// Calculate current streak
		int currentStreak = calculateCurrentStreak(journalDates);

		// Calculate longest streak
		int longestStreak = calculateLongestStreak(journalDates);

		return new JournalStats(totalJournals, currentStreak, longestStreak);
	}

	private int calculateCurrentStreak(TreeSet<LocalDateTime> dates) {
		if (dates.isEmpty())
			return 0;

		LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
		LocalDateTime lastDate = dates.first(); // Most recent date

		// If last journal is not from today or yesterday, streak is 0
		if (ChronoUnit.DAYS.between(lastDate, today) > 1) {
			return 0;
		}

		int streak = 1;
		LocalDateTime currentDate = lastDate;

		for (LocalDateTime date : dates.tailSet(lastDate, false)) {
			if (ChronoUnit.DAYS.between(date, currentDate) == 1) {
				streak++;
				currentDate = date;
			} else {
				break;
			}
		}

		return streak;
	}

	private int calculateLongestStreak(TreeSet<LocalDateTime> dates) {
		if (dates.isEmpty())
			return 0;

		int longestStreak = 1;
		int currentStreak = 1;
		LocalDateTime previousDate = null;

		for (LocalDateTime date : dates) {
			if (previousDate != null) {
				if (ChronoUnit.DAYS.between(date, previousDate) == 1) {
					currentStreak++;
					longestStreak = Math.max(longestStreak, currentStreak);
				} else {
					currentStreak = 1;
				}
			}
			previousDate = date;
		}

		return longestStreak;
	}
}
