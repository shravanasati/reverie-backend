package com.reverie.reverie.service;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.tokenizer.core.WhiteSpaceWordTokenizer;
import com.kennycason.kumo.palette.ColorPalette;
import com.reverie.reverie.model.Journal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WordCloudService {

	@Autowired
	private JournalService journalService;

	/**
	 * Generates a word cloud image from the provided list of texts.
	 *
	 * @param texts The list of strings to analyze for word frequencies.
	 * @return A byte array containing the PNG image data of the word cloud.
	 * @throws IOException If there is an error writing the image data.
	 */
	public byte[] generateWordCloudImage(List<String> texts) throws IOException {
		if (texts == null || texts.isEmpty()) {
			// Handle empty input gracefully, maybe return a default image or throw
			// exception
			// For now, returning an empty byte array
			System.err.println("Input text list is null or empty.");
			return new byte[0]; // Or throw new IllegalArgumentException("Input text cannot be empty");
		}

		// --- 1. Setup Frequency Analyzer ---
		final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
		frequencyAnalyzer.setWordFrequenciesToReturn(600);
		frequencyAnalyzer.setMinWordLength(4);
		frequencyAnalyzer.setWordTokenizer(new WhiteSpaceWordTokenizer());
		// Optional: Add custom stop words if needed
		Set<String> customStopWords = new HashSet<>(Arrays.asList(
				"the", "and", "that", "have", "for", "not", "with", "you", "this", "but",
				"his", "from", "they", "she", "will", "would", "there", "their", "what",
				"about", "which", "when", "make", "like", "time", "just", "know", "take",
				"into", "year", "your", "good", "some", "could", "them", "see", "other",
				"than", "then", "now", "only", "come", "its", "also", "back",
				"after", "use", "two", "how", "our", "work", "well", "way", "even", "new",
				"want", "because", "any", "these", "give", "most"));
		frequencyAnalyzer.setStopWords(customStopWords);
		// frequencyAnalyzer.setStopWords(new StopWordFilter(texts)); // Use default
		// English stop words

		// --- 2. Calculate Word Frequencies ---
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(texts);

		// --- 3. Configure Word Cloud Appearance ---
		final Dimension dimension = new Dimension(600, 300);
		final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new RectangleBackground(dimension));
		wordCloud.setBackgroundColor(Color.WHITE);
		wordCloud.setColorPalette(new ColorPalette(
				new Color(0x2E4053), // Dark Blue/Grey
				new Color(0x5D6D7E), // Medium Grey
				new Color(0x85929E), // Lighter Grey
				new Color(0x17A589), // Teal
				new Color(0x48C9B0) // Light Teal
		));
		wordCloud.setFontScalar(new LinearFontScalar(12, 45)); // Min/Max font size
		wordCloud.setKumoFont(new KumoFont("SansSerif", FontWeight.BOLD));

		// --- 4. Build Word Cloud ---
		wordCloud.build(wordFrequencies);

		// --- 5. Output Image to Byte Array ---
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			wordCloud.writeToStreamAsPNG(baos); // Write PNG data to the stream
			return baos.toByteArray(); // Return the image data as bytes
		}
	}

	/**
	 * Overloaded method to generate a word cloud from a single block of text.
	 * Splits the text into lines for processing.
	 *
	 * @param singleText The single string containing the text.
	 * @return A byte array containing the PNG image data of the word cloud.
	 * @throws IOException If there is an error writing the image data.
	 */
	public byte[] generateWordCloudImage(String singleText) throws IOException {
		if (singleText == null || singleText.trim().isEmpty()) {
			System.err.println("Input text string is null or empty.");
			return new byte[0]; // Or throw new IllegalArgumentException("Input text cannot be empty");
		}
		// Split the single text block into lines, treating it like a list of strings
		List<String> texts = Arrays.asList(singleText.split("\\r?\\n")); // Split by newline
		return generateWordCloudImage(texts);
	}

	/**
	 * Generates a word cloud image from the user's journals.
	 *
	 * @param userId The ID of the user whose journals are to be processed.
	 * @return A byte array containing the PNG image data of the word cloud.
	 * @throws IOException If there is an error writing the image data.
	 */
	public byte[] generateWordCloudFromUserJournals(String userId) throws IOException {
		List<Journal> userJournals = journalService.getUserJournals(userId);

		if (userJournals.isEmpty()) {
			throw new IllegalArgumentException("No journals found for user: " + userId);
		}

		// Extract text content from journals
		List<String> journalTexts = userJournals.stream()
				.map(journal -> journal.getContent())
				.collect(Collectors.toList());

		// Use existing method to generate word cloud
		return generateWordCloudImage(journalTexts);
	}
}
