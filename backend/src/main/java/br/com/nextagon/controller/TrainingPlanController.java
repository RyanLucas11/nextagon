package br.com.nextagon.controller;

import br.com.nextagon.dto.request.TrainingPlanRequestDto;
import br.com.nextagon.dto.response.TrainingPlanResponseDto;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.TrainingPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/training-plans")
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    // Apenas PROFESSIONAL cria plano
    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<TrainingPlanResponseDto> createPlan(
            @Valid @RequestBody TrainingPlanRequestDto dto) {

        String professionalId = AuthenticatedUser.getId();
        var plan = trainingPlanService.createPlan(professionalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TrainingPlanResponseDto(plan));
    }

    // Atleta e profissional do contrato podem visualizar
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<TrainingPlanResponseDto>> getPlansByContract(
            @PathVariable String contractId) {

        String userId = AuthenticatedUser.getId();
        return ResponseEntity.ok(
                trainingPlanService.getPlansByContract(contractId, userId)
                        .stream()
                        .map(TrainingPlanResponseDto::new)
                        .toList()
        );
    }

    // Apenas PROFESSIONAL atualiza
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<TrainingPlanResponseDto> updatePlan(
            @PathVariable String planId,
            @Valid @RequestBody TrainingPlanRequestDto dto) {

        String professionalId = AuthenticatedUser.getId();
        var plan = trainingPlanService.updatePlan(planId, professionalId, dto);
        return ResponseEntity.ok(new TrainingPlanResponseDto(plan));
    }

    // Apenas PROFESSIONAL desativa
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String planId) {
        String professionalId = AuthenticatedUser.getId();
        trainingPlanService.deactivatePlan(planId, professionalId);
        return ResponseEntity.noContent().build();
    }
}