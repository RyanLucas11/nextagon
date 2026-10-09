package br.com.nextagon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostRequestDto {

    @NotBlank
    private String feed;

    @NotBlank
    private String type;

    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 1200)
    private String text;

    @Size(max = 60)
    private String category;

    @Size(max = 120)
    private String location;

    @Size(max = 40)
    private String employmentType;
}
