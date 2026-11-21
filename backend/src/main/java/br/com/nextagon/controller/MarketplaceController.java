
package br.com.nextagon.controller;

import br.com.nextagon.dto.response.ProfessionalResponseDto;
import br.com.nextagon.model.Role;
import br.com.nextagon.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final UserRepository userRepository;

 @GetMapping("/professionals")
@Transactional(readOnly = true)
public ResponseEntity<Page<ProfessionalResponseDto>> getProfessionals(
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O número da página não pode ser negativo"
            );
        }

        if (size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e 50"
            );
        }

        if (!Double.isFinite(minRating)
                || minRating < 0
                || minRating > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A avaliação mínima deve estar entre 0 e 5"
            );
        }

        PageRequest pageable = PageRequest.of(page, size);

        Page<ProfessionalResponseDto> professionals =
                userRepository.findProfessionalsForMarketplace(
                        Role.PROFESSIONAL,
                        minRating,
                        pageable
                ).map(ProfessionalResponseDto::new);

        return ResponseEntity.ok(professionals);
    }
}
