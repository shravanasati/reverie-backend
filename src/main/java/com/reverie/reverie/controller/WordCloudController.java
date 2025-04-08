package com.reverie.reverie.controller;

import com.reverie.reverie.service.WordCloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/wordcloud") // Base path for word cloud endpoints
public class WordCloudController {

	private final WordCloudService wordCloudService;

	// Constructor injection is preferred for required dependencies
	@Autowired
	public WordCloudController(WordCloudService wordCloudService) {
		this.wordCloudService = wordCloudService;
	}

	/**
	 * Endpoint to generate a word cloud from text provided in the request body.
	 * Expects a JSON array of strings, e.g., ["text line 1", "text line 2"]
	 * or a single plain text string.
	 *
	 * @param textInput The text content (either List<String> or String) from the
	 *                  request body.
	 * @return ResponseEntity containing the PNG image bytes or an error status.
	 */
	@PostMapping(produces = MediaType.IMAGE_PNG_VALUE) // Specify this endpoint produces PNG
	public ResponseEntity<byte[]> createWordCloud(@RequestBody String textInput) {
		// Check if input is empty or null
		if (textInput == null || textInput.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Input text cannot be empty.".getBytes());
		}

		try {
			// Generate the word cloud using the service
			// Use the overloaded service method that accepts a single String
			byte[] imageBytes = wordCloudService.generateWordCloudImage(textInput);

			if (imageBytes.length == 0) {
				// Handle case where service returned empty bytes (e.g., no valid words found)
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
			}

			// Set headers for the response
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			headers.setContentLength(imageBytes.length);
			// Optional: Suggest a filename for download
			// headers.setContentDispositionFormData("attachment", "wordcloud.png");

			// Return the image bytes with OK status
			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

		} catch (IOException e) {
			// Log the exception (using a proper logger is recommended)
			System.err.println("Error generating word cloud: " + e.getMessage());
			// Return an internal server error status
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (IllegalArgumentException e) {
			// Handle specific exceptions like invalid input if thrown by the service
			System.err.println("Invalid input for word cloud: " + e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage().getBytes());
		}
	}

	/**
	 * Example GET endpoint (optional) - less suitable for large text inputs.
	 * Takes text as a request parameter.
	 *
	 * @param text The text content from the request parameter.
	 * @return ResponseEntity containing the PNG image bytes or an error status.
	 */
	@GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> createWordCloudFromParam(
			@RequestParam(value = "text", defaultValue = "") String text) {
		if (text.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Input text parameter cannot be empty.".getBytes());
		}
		// Reuse the POST logic by wrapping the call
		// Note: GET requests have URL length limits, making POST better for larger
		// texts
		return createWordCloud(text);
	}

	/**
	 * Endpoint to generate a word cloud from user's journals.
	 *
	 * @param userId The ID of the user whose journals will be used.
	 * @return ResponseEntity containing the PNG image bytes or an error status.
	 */
	@GetMapping(value = "/user/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> createWordCloudFromUserJournals(@PathVariable String userId) {
		try {
			byte[] imageBytes = wordCloudService.generateWordCloudFromUserJournals(userId);

			if (imageBytes.length == 0) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			headers.setContentLength(imageBytes.length);

			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			System.err.println("Error generating word cloud for user: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid input for user word cloud: " + e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage().getBytes());
		}
	}
}
