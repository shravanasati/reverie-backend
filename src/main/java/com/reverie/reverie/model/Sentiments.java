package com.reverie.reverie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journal_sentiments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sentiments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "journal_id", nullable = false, unique = true)
    private Journal journal;

    @Column(nullable = false)
    private String label; // e.g., POSITIVE, NEGATIVE

    @Column(nullable = false)
    private Double score; // will be negative if label is NEGATIVE
}
