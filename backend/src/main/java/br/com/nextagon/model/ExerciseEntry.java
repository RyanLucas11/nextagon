package br.com.nextagon.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @Column(nullable = false)
    private String name;

    private Integer sets;
    private String reps;
    private Double weightKg;
    private Integer restSecs;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String videoUrl;
    private int orderIndex;
}