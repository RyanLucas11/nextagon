package br.com.nextagon.controller;

import br.com.nextagon.model.Contract;
import br.com.nextagon.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/request")
    public ResponseEntity<Contract> requestContract(
            @RequestParam String athleteId,
            @RequestParam String professionalId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contractService.createContractRequest(athleteId, professionalId));
    }

    @PatchMapping("/{contractId}/respond")
    public ResponseEntity<Contract> respondContract(
            @PathVariable String contractId,
            @RequestParam String professionalId,
            @RequestParam boolean accept) {
        return ResponseEntity.ok(contractService.respondToContract(contractId, professionalId, accept));
    }

    @PatchMapping("/{contractId}/complete")
    public ResponseEntity<Contract> completeContract(
            @PathVariable String contractId,
            @RequestParam String professionalId) {
        return ResponseEntity.ok(contractService.completeContract(contractId, professionalId));
    }

    @GetMapping("/athlete/{athleteId}")
    public ResponseEntity<List<Contract>> getAthleteContracts(@PathVariable String athleteId) {
        return ResponseEntity.ok(contractService.getContractsByAthlete(athleteId));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<Contract>> getProfessionalContracts(@PathVariable String professionalId) {
        return ResponseEntity.ok(contractService.getContractsByProfessional(professionalId));
    }
}