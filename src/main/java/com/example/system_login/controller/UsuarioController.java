package com.example.system_login.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.system_login.dto.CadastroRequestDTO;
import com.example.system_login.dto.LoginRequestDTO;
import com.example.system_login.dto.SenhaRequestDTO;
import com.example.system_login.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UsuarioController {
    
    private final UsuarioService service;

    @PostMapping("/cadastrar")
    public ResponseEntity<String> cadastrar(@RequestBody CadastroRequestDTO dto){
        try{
            service.cadastrarUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuário cadastrado com sucesso!");
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDTO dto){
    
        String token = service.autenticarUsuario(dto);
        
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body("Login realizado com sucesso!");
            

    }

    @PostMapping("/atualizar-senha")
    public ResponseEntity<String> atualizarSenha(@RequestBody SenhaRequestDTO dto, Authentication authentication){
    
        String username = authentication.getName();
        try{
            service.atualizarSenha(username, dto);
            return ResponseEntity.ok("Senha atualizada com sucesso!");
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
