package ru.practicum.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private int id;
    @NotBlank(message = "name cannot be blank")
    @Size(min = 2, max = 250)
    private String name;
    @NotBlank(message = "email cannot be blank")
    @Email
    @Size(min = 6, max = 254)
    private String email;
}
