package br.com.nextagon.controller;

import br.com.nextagon.dto.response.ProfessionalResponseDto;
import br.com.nextagon.model.Role;
import br.com.nextagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final UserRepository userRepository;

    @GetMapping("/professionals")
    public ResponseEntity<Page<ProfessionalResponseDto>> getProfessionals(
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, Math.min(size, 50));
        var result = userRepository
                .findProfessionalsForMarketplace(Role.PROFESSIONAL, minRating, pageable)
                .map(ProfessionalResponseDto::new);

        return ResponseEntity.ok(result);
    }
}