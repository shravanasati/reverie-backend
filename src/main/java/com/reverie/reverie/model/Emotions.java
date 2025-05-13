package com.reverie.reverie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journal_emotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emotions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    @Column(nullable = false)
    private String label; // e.g., "joy", "neutral", "surprise"

    @Column(nullable = false)
    private Double score;
}
