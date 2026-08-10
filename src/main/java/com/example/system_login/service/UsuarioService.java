package com.example.system_login.service;

import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.system_login.dto.CadastroRequestDTO;
import com.example.system_login.dto.LoginRequestDTO;
import com.example.system_login.dto.SenhaRequestDTO;
import com.example.system_login.exception.TooManyRequestsException;
import com.example.system_login.model.Usuario;
import com.example.system_login.repository.UsuarioRepository;
import com.example.system_login.util.JwtUtil;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService{

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
    
    public String autenticarUsuario(LoginRequestDTO login){
        LocalDateTime now = LocalDateTime.now();
        Usuario usuario = repository.findByUsername(login.username()).orElseThrow(() -> new IllegalArgumentException("Usuário inexistente ou senha inválida."));


        if(usuario.getTimeLocked() != null &&usuario.getTimeLocked().isAfter(now)){
            throw new TooManyRequestsException("Conta bloqueada por várias tentativas.");
        }

        if(!passwordEncoder.matches(login.password(), usuario.getPassword())){
            tentativasLogin(usuario);

            repository.saveAndFlush(usuario);

            throw new IllegalArgumentException("Usuário inexistente ou senha inválida.");
        }

        usuario.setTentativas(0);
        usuario.setLocked(false);
        usuario.setTimeLocked(null);
        repository.save(usuario);

        return jwtUtil.generateToken(usuario.getUsername());
        
    }

    private void tentativasLogin(Usuario usuario){
        Integer novasTetativas = usuario.getTentativas() + 1;
        usuario.setTentativas(novasTetativas);

        if(novasTetativas >= 5){
            usuario.setLocked(true);
            usuario.setTimeLocked(LocalDateTime.now().plusMinutes(5)); 
        }
        repository.saveAndFlush(usuario);
    }
    
    @Transactional
    public void atualizarSenha(String username, SenhaRequestDTO dto){ 
        Usuario usuario = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        
        if(!passwordEncoder.matches(dto.old_password(), usuario.getPassword())){
            throw new RuntimeException("Senha antiga incorreta.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.new_password()));
        repository.save(usuario);
    }
}
