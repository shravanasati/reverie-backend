package com.reverie.reverie.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Journal {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(nullable = false)
        private String title;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String content;

        @Column(name = "emotion")
        private String emotion;

        @Column(name = "sentiment_score")
        private Double sentimentScore;

        @Column(nullable = false)
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime createdAt;

}

