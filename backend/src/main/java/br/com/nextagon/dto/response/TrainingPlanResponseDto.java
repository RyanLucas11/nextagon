package br.com.nextagon.dto.response;

import br.com.nextagon.model.ExerciseEntry;
import br.com.nextagon.model.TrainingPlan;
import br.com.nextagon.model.TrainingSession;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TrainingPlanResponseDto {

    private final String id;
    private final String contractId;
    private final String title;
    private final String description;
    private final boolean active;
    private final List<SessionDto> sessions;
    private final LocalDateTime createdAt;

    public TrainingPlanResponseDto(TrainingPlan plan) {
        this.id = plan.getId();
        this.contractId = plan.getContract().getId();
        this.title = plan.getTitle();
        this.description = plan.getDescription();
        this.active = plan.isActive();
        this.createdAt = plan.getCreatedAt();
        this.sessions = plan.getSessions() == null ? List.of() :
                plan.getSessions().stream().map(SessionDto::new).toList();
    }

    @Getter
    public static class SessionDto {
        private final String id;
        private final String title;
        private final int dayOfWeek;
        private final String notes;
        private final List<ExerciseDto> exercises;

        public SessionDto(TrainingSession s) {
            this.id = s.getId();
            this.title = s.getTitle();
            this.dayOfWeek = s.getDayOfWeek();
            this.notes = s.getNotes();
            this.exercises = s.getExercises() == null ? List.of() :
                    s.getExercises().stream().map(ExerciseDto::new).toList();
        }
    }

    @Getter
    public static class ExerciseDto {
        private final String id;
        private final String name;
        private final Integer sets;
        private final String reps;
        private final Double weightKg;
        private final Integer restSecs;
        private final String notes;
        private final String videoUrl;

        public ExerciseDto(ExerciseEntry e) {
            this.id = e.getId();
            this.name = e.getName();
            this.sets = e.getSets();
            this.reps = e.getReps();
            this.weightKg = e.getWeightKg();
            this.restSecs = e.getRestSecs();
            this.notes = e.getNotes();
            this.videoUrl = e.getVideoUrl();
        }
    }
}