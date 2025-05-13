package com.reverie.reverie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "keyword_journal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(JournalKeywordId.class)
public class Journal_keywords {
    @Id
    @ManyToOne
    @JoinColumn(name = "journal_id")
    private Journal journal;

    @Id
    @ManyToOne
    @JoinColumn(name = "keyword_id")
    private Keywords keyword;
}