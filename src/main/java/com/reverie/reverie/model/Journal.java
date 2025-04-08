package com.reverie.reverie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
@Entity
@Table(name = "journal_entries",
uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "title"}))
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

        @Column(nullable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

}

