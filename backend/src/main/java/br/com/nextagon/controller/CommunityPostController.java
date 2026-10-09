package br.com.nextagon.controller;

import br.com.nextagon.dto.request.CommunityPostRequestDto;
import br.com.nextagon.dto.response.CommunityPostResponseDto;
import br.com.nextagon.security.AuthenticatedUser;
import br.com.nextagon.service.CommunityPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @GetMapping
    public ResponseEntity<List<CommunityPostResponseDto>> list(@RequestParam String feed) {
        return ResponseEntity.ok(communityPostService.list(feed, AuthenticatedUser.getId()));
    }

    @PostMapping
    public ResponseEntity<CommunityPostResponseDto> create(
            @Valid @RequestBody CommunityPostRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityPostService.create(request, AuthenticatedUser.getId()));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<CommunityPostResponseDto> toggleLike(@PathVariable String postId) {
        return ResponseEntity.ok(communityPostService.toggleLike(postId, AuthenticatedUser.getId()));
    }
}
