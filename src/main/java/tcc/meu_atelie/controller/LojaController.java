package tcc.meu_atelie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tcc.meu_atelie.dto.LojaDTO;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.services.LojaService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class LojaController {
    private final LojaService lojaService;

    @GetMapping("/{idUsuario}")
    public ResponseEntity<LojaDTO> exibirPerfil(@PathVariable Long idUsuario, Principal principal) {
        Loja loja = lojaService.buscarPerfil(idUsuario);

        String emailDoToken = principal.getName();

        LojaDTO dto = new LojaDTO(
                loja.getIdLoja(),
                loja.getNome(),
                emailDoToken,
                loja.getDescricao(),
                loja.getCnpj(),
                loja.getWhatsapp(),
                loja.getFacebook(),
                loja.getInstagram(),
                loja.getUsuario().getIdUsuario(),
                loja.getUsuario().getNome(),
                loja.getFotoUrl()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{idUsuario}/foto")
    public ResponseEntity<?> atualizarFoto(@PathVariable Long idUsuario, @RequestParam("arquivo") MultipartFile arquivo) {
        String nomeArquivo = lojaService.salvarFoto(idUsuario, arquivo);
        return ResponseEntity.ok(Map.of("fotoUrl", nomeArquivo));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<LojaDTO> editarPerfil(@PathVariable Long idUsuario, @RequestBody LojaDTO dto, Principal principal) {
        Loja dadosAtualizados = new Loja();
        dadosAtualizados.setNome(dto.getNome());
        dadosAtualizados.setDescricao(dto.getDescricao());
        dadosAtualizados.setWhatsapp(dto.getWhatsapp());
        dadosAtualizados.setFacebook(dto.getFacebook());
        dadosAtualizados.setInstagram(dto.getInstagram());

        Loja lojaSalva = lojaService.atualizarPerfil(idUsuario, dadosAtualizados);
        String emailDoToken = principal.getName();

        LojaDTO resultadoDto = new LojaDTO(
                lojaSalva.getIdLoja(),
                lojaSalva.getNome(),
                emailDoToken,
                lojaSalva.getDescricao(),
                lojaSalva.getCnpj(),
                lojaSalva.getWhatsapp(),
                lojaSalva.getFacebook(),
                lojaSalva.getInstagram(),
                lojaSalva.getUsuario().getIdUsuario(),
                lojaSalva.getUsuario().getNome(),
                lojaSalva.getFotoUrl()
        );

        return ResponseEntity.ok(resultadoDto);
    }
}
