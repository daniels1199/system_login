package com.example.system_login.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastroRequestDTO (
    @NotBlank(message = "O nome do usuário é obrigtório!")
    String username,
    @NotBlank(message = "A senha é obrigatória!") 
    String password){}
