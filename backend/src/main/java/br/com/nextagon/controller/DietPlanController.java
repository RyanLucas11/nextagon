package br.com.nextagon.controller;

import br.com.nextagon.dto.request.DietPlanRequestDto;
import br.com.nextagon.dto.response.DietPlanResponseDto;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.DietPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanService dietPlanService;

    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<DietPlanResponseDto> createPlan(
            @Valid @RequestBody DietPlanRequestDto dto) {

        String professionalId = AuthenticatedUser.getId();
        var plan = dietPlanService.createPlan(professionalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DietPlanResponseDto(plan));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<DietPlanResponseDto>> getPlansByContract(
            @PathVariable String contractId) {

        String userId = AuthenticatedUser.getId();
        return ResponseEntity.ok(
                dietPlanService.getPlansByContract(contractId, userId)
                        .stream()
                        .map(DietPlanResponseDto::new)
                        .toList()
        );
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<DietPlanResponseDto> updatePlan(
            @PathVariable String planId,
            @Valid @RequestBody DietPlanRequestDto dto) {

        String professionalId = AuthenticatedUser.getId();
        var plan = dietPlanService.updatePlan(planId, professionalId, dto);
        return ResponseEntity.ok(new DietPlanResponseDto(plan));
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String planId) {
        String professionalId = AuthenticatedUser.getId();
        dietPlanService.deactivatePlan(planId, professionalId);
        return ResponseEntity.noContent().build();
    }
}