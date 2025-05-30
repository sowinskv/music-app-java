package com.example.mp5jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // JOINED Strategy [cite: 2]
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractMediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    public AbstractMediaItem(String title) {
        this.title = title;
    }

    // Justification for JOINED:
    // Pros: Normalized, no null columns, clear which type each row is.
    // Cons: Requires joins for polymorphic queries (can impact performance).
    // Counter-example (SINGLE_TABLE)[cite: 3]: Would put all Song and Album
    // columns in one table. This leads to many NULL values and can be
    // inefficient and unclear, especially if Song and Album have many
    // distinct attributes.
}