package br.com.nextagon.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "athlete_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AthleteProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double height;
    private Double weight;

    @ElementCollection
    @CollectionTable(name = "athlete_goals", joinColumns = @JoinColumn(name = "athlete_id"))
    @Column(name = "goal")
    @Builder.Default
    private List<String> goals = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String healthNotes;
    // Adicionar após healthNotes:
    private String fitnessLevel; // beginner, intermediate, advanced
}