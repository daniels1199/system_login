package com.example.system_login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.system_login.dto.CadastroRequestDTO;
import com.example.system_login.dto.LoginRequestDTO;
import com.example.system_login.service.UsuarioService;

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
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO user){
        

        String response = service.autenticarUsuario(user);

        if(response.contains("Usuário inexistente ou senha inválida")){
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);

    }
}
