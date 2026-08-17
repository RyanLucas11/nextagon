package br.com.nextagon.dto.response;

import br.com.nextagon.model.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {

    private final String id;
    private final String contractId;
    private final String reviewerId;
    private final String reviewerName;
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.contractId = review.getContract().getId();
        this.reviewerId = review.getReviewer().getId();
        this.reviewerName = review.getReviewer().getName();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }
}