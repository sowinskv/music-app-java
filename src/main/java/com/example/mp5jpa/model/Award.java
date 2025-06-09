package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Award {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Award name cannot be blank.")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Presenter cannot be blank.")
    @Column(nullable = false)
    private String presenter;


    @OneToMany(mappedBy = "award", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AwardGrant> awardGrants = new HashSet<>();

    public Award(String name, String presenter) {
        this.name = name;
        this.presenter = presenter;
    }

    @Override
    public String toString() {
        return "Award [id=" + id + ", name='" + name + "', presenter='" + presenter + "']";
    }
}