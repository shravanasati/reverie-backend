package com.reverie.reverie.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"user\"") // Added quotes to escape the reserved keyword
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
        @Id
        private String id;

        @Column(nullable = false)
        private String name;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(name = "email_verified", nullable = false)
        private boolean emailVerified;

        private String image;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @PrePersist
        public void ensureId() {
                if (this.id == null) {
                        this.id = UUID.randomUUID().toString();
                }
        }

}
