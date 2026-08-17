package br.com.nextagon.controller;

import br.com.nextagon.dto.request.ReviewRequestDto;
import br.com.nextagon.dto.response.ReviewResponseDto;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Apenas ATHLETE pode avaliar
    @PostMapping
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto dto) {

        String reviewerId = AuthenticatedUser.getId();
        var review = reviewService.createReview(dto.getContractId(), reviewerId, dto.getRating(), dto.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReviewResponseDto(review));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ReviewResponseDto>> getProfessionalReviews(
            @PathVariable String professionalId) {

        return ResponseEntity.ok(
                reviewService.getReviewsByProfessional(professionalId)
                        .stream()
                        .map(ReviewResponseDto::new)
                        .toList()
        );
    }
}