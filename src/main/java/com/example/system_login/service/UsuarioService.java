package com.example.system_login.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.system_login.dto.CadastroRequestDTO;
import com.example.system_login.dto.LoginRequestDTO;
import com.example.system_login.exception.TooManyRequestsException;
import com.example.system_login.model.Usuario;
import com.example.system_login.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService{

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public void cadastrarUsuario(CadastroRequestDTO dto){
        if(repository.findByUsername(dto.username()).isPresent()){
            throw new RuntimeException("Erro ao processar o cadastro. Verifique os dados informados ou tente recuperar sua senha.");
        }
        else{
            Usuario novoUsuario = new Usuario();
            novoUsuario.setUsername(dto.username());
            novoUsuario.setPassword(passwordEncoder.encode(dto.password()));
            repository.save(novoUsuario);
        }
    }
    
    @Transactional
    public String autenticarUsuario(LoginRequestDTO login){
        LocalDateTime now = LocalDateTime.now();
        Usuario usuario = repository.findByUsername(login.username()).orElseThrow(() -> new IllegalArgumentException("Usuário inexistente ou senha inválida."));


        if(usuario.getTimeLocked() != null &&usuario.getTimeLocked().isAfter(now)){
            throw new TooManyRequestsException("Conta bloqueada por várias tentativas.");
        }

        try{
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.username(), login.password())
            );
            
            
            usuario.setTentativas(0);
            usuario.setLocked(false);
            usuario.setTimeLocked(null);
            repository.save(usuario);

            return "Login efetuado com sucesso!";

        }catch(AuthenticationException e){
            tentativasLogin(usuario);
            return e.getMessage();
        }
    }

    @Transactional
    private void tentativasLogin(Usuario usuario){
        Integer novasTetativas = usuario.getTentativas() + 1;
        usuario.setTentativas(novasTetativas);

        if(novasTetativas >= 5){
            usuario.setLocked(true);
            usuario.setTimeLocked(LocalDateTime.now().plusMinutes(5));
            repository.save(usuario); 
        }
    }    
}
