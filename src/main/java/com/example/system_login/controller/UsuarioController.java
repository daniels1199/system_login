package com.example.system_login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.system_login.dto.CadastroRequestDTO;
import com.example.system_login.dto.LoginRequestDTO;
import com.example.system_login.dto.SenhaRequestDTO;
import com.example.system_login.service.UsuarioService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDTO dto, HttpServletResponse response, HttpServletRequest request){
    
        String token = service.autenticarUsuario(dto);

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken(); // Esta chamada força o Hibernate/Spring a criar o valor físico
        }
        
        Cookie cookie = new Cookie("AUTH_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //Ativa segurança HTTPS, mas desativa o H2 Console
        cookie.setPath("/");
        cookie.setMaxAge(7200);

        response.addCookie(cookie);
        response.addHeader("Set Cookie", "AUTH_TOKEN=" + token + "; HttpOnly; Secure; Path=/; Max-Age=7200; SameSite=Strict");

        return ResponseEntity.ok("Login efetuado com sucesso!");
            

    }

    @PutMapping("/atualizar-senha")
    public ResponseEntity<String> atualizarSenha(@RequestBody SenhaRequestDTO dto, Authentication authentication){
    
        String username = authentication.getName();
        try{
            service.atualizarSenha(username, dto);
            return ResponseEntity.ok("Senha atualizada com sucesso!");
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        
        // Invalida o cookie do JWT colocando o Max-Age como 0
        Cookie jwtCookie = new Cookie("AUTH_TOKEN", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); 
        
        // Invalida o cookie do CSRF colocando o Max-Age como 0
        Cookie csrfCookie = new Cookie("XSRF-TOKEN", null);
        csrfCookie.setHttpOnly(false);
        csrfCookie.setPath("/");
        csrfCookie.setMaxAge(0); 
        
        response.addCookie(jwtCookie);
        response.addCookie(csrfCookie);
        
        // Reforça a destruição dos cookies nos navegadores via cabeçalho
        response.addHeader("Set-Cookie", "AUTH_TOKEN=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict");
        response.addHeader("Set-Cookie", "XSRF-TOKEN=; Max-Age=0; Path=/; SameSite=Strict");

        return ResponseEntity.ok("Logout efetuado com sucesso! Sessão encerrada.");
    }
}
