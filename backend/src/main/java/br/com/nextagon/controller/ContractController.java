
package br.com.nextagon.controller;

import br.com.nextagon.dto.request.ContractRequestDto;
import br.com.nextagon.dto.response.ContractResponseDto;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/request")
    public ResponseEntity<ContractResponseDto> requestContract(
            @Valid @RequestBody ContractRequestDto dto) {

        String athleteId = AuthenticatedUser.getId();

        ContractResponseDto response = new ContractResponseDto(
                contractService.createContractRequest(
                        athleteId,
                        dto.getProfessionalId(),
                        dto.getMessage()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{contractId}/respond")
    public ResponseEntity<ContractResponseDto> respondContract(
            @PathVariable String contractId,
            @RequestParam boolean accept) {

        String professionalId = AuthenticatedUser.getId();

        return ResponseEntity.ok(
                new ContractResponseDto(
                        contractService.respondToContract(
                                contractId,
                                professionalId,
                                accept
                        )
                )
        );
    }

    @PatchMapping("/{contractId}/complete")
    public ResponseEntity<ContractResponseDto> completeContract(
            @PathVariable String contractId) {

        String professionalId = AuthenticatedUser.getId();

        return ResponseEntity.ok(
                new ContractResponseDto(
                        contractService.completeContract(
                                contractId,
                                professionalId
                        )
                )
        );
    }

    @GetMapping("/athlete/{athleteId}")
    public ResponseEntity<List<ContractResponseDto>> getAthleteContracts(
            @PathVariable String athleteId) {

        String authenticatedId = AuthenticatedUser.getId();

        if (!authenticatedId.equals(athleteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode consultar contratos de outro atleta"
            );
        }

        return ResponseEntity.ok(
                contractService.getContractsByAthlete(authenticatedId)
                        .stream()
                        .map(ContractResponseDto::new)
                        .toList()
        );
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ContractResponseDto>> getProfessionalContracts(
            @PathVariable String professionalId) {

        String authenticatedId = AuthenticatedUser.getId();

        if (!authenticatedId.equals(professionalId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode consultar contratos de outro profissional"
            );
        }

        return ResponseEntity.ok(
                contractService.getContractsByProfessional(authenticatedId)
                        .stream()
                        .map(ContractResponseDto::new)
                        .toList()
        );
    }
}
