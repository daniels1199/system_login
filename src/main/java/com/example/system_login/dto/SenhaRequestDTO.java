package com.example.system_login.dto;

import jakarta.validation.constraints.NotBlank;

public record SenhaRequestDTO(
    @NotBlank(message = "A senha antiga é obrigatória!")
    String old_password,
    @NotBlank(message = "A nova senha é obrigatória!")
    String new_password) {}
