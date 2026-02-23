package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.auth.TokenService;

import tcc.meu_atelie.dto.UsuarioDTO;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.services.UsuarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenService tokenService;


    @PostMapping
    public ResponseEntity<?> criarUsuario(@RequestBody UsuarioDTO dto) {
        try {

            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setEmail(dto.getEmail());
            usuario.setTelefone(dto.getTelefone());
            usuario.setCpf(dto.getCpf());
            usuario.setSenha(dto.getSenha());

            Usuario salvo = usuarioService.salvarUsuario(usuario);

            UsuarioDTO response = new UsuarioDTO(salvo);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Este e-mail já está cadastrado."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao cadastrar usuário."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> fazerLogin(@RequestBody UsuarioDTO usuarioDTO) {

        Usuario usuarioAutenticado = usuarioService.autenticar(
                usuarioDTO.getEmail(),
                usuarioDTO.getSenha()
        );

        if (usuarioAutenticado != null) {

            String jwtToken = tokenService.gerarToken(usuarioAutenticado);

            UsuarioDTO usuarioAutenticadoDTO = new UsuarioDTO(
                    usuarioAutenticado.getIdUsuario(),
                    usuarioAutenticado.getNome(),
                    usuarioAutenticado.getEmail(),
                    usuarioAutenticado.getTelefone(),
                    usuarioAutenticado.getCpf(),
                    null,
                    jwtToken
            );


            return ResponseEntity.ok(usuarioAutenticadoDTO);

        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }



    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarUsuarios()
                .stream()
                .map(UsuarioDTO::new)
                .toList();
    }
}

