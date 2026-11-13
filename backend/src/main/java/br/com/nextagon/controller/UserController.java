
package br.com.nextagon.controller;

import br.com.nextagon.dto.UserSummaryDto;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> getCurrentUser() {

        String userId = AuthenticatedUser.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário não encontrado"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Conta inativa"
            );
        }

        UserSummaryDto response = new UserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }
}
