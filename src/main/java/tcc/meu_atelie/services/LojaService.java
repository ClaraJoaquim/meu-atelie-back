package tcc.meu_atelie.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.repositories.LojaRepository;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository lojaRepository;

    public Loja buscarPerfil(Long idUsuario) {
        return lojaRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil da loja não encontrado."));
    }

    public Loja atualizarPerfil(Long idUsuario, Loja dadosAtualizados) {
        Loja loja = buscarPerfil(idUsuario);

        loja.setNome(dadosAtualizados.getNome());
        loja.setDescricao(dadosAtualizados.getDescricao());
        loja.setWhatsapp(dadosAtualizados.getWhatsapp());
        loja.setFacebook(dadosAtualizados.getFacebook());
        loja.setInstagram(dadosAtualizados.getInstagram());

        return lojaRepository.save(loja);
    }

    public String salvarFoto(Long idUsuario, MultipartFile arquivo) {
        try {
            String nomeArquivo = UUID.randomUUID().toString() + "_" + arquivo.getOriginalFilename();

            Path diretorio = Paths.get("uploads");
            if (!Files.exists(diretorio)) {
                Files.createDirectories(diretorio);
            }

            Path caminhoArquivo = diretorio.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), caminhoArquivo);

            Loja loja = buscarPerfil(idUsuario);
            loja.setFotoUrl(nomeArquivo);
            lojaRepository.save(loja);

            return nomeArquivo;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar a foto", e);
        }
    }
}
