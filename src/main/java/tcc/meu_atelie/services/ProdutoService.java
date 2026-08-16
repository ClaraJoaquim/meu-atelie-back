package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tcc.meu_atelie.dto.ProdutoDTO;
import tcc.meu_atelie.models.Categoria;
import tcc.meu_atelie.models.Material;
import tcc.meu_atelie.models.Produto;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.CategoriaRepository;
import tcc.meu_atelie.repositories.MaterialRepository;
import tcc.meu_atelie.repositories.ProdutoRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProdutoService {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MaterialRepository materialRepository;

    public ProdutoDTO cadastrar(ProdutoDTO dto, MultipartFile arquivoImagem) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setPrecoCusto(dto.getPrecoCusto());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setStatus(dto.getStatus());
        produto.setLargura(dto.getLargura());
        produto.setAltura(dto.getAltura());
        produto.setObservacoes(dto.getObservacoes());

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        produto.setCategoria(categoria);

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        produto.setUsuario(usuario);

        if (dto.getMateriaisIds() != null && !dto.getMateriaisIds().isEmpty()) {
            List<Material> materiais = materialRepository.findAllById(dto.getMateriaisIds());
            produto.setMateriais(materiais);
        }

        if (arquivoImagem != null && !arquivoImagem.isEmpty()) {
            try {
                String nomeArquivo = UUID.randomUUID() + "_" + arquivoImagem.getOriginalFilename();
                Path caminho = Paths.get("uploads/" + nomeArquivo);
                Files.createDirectories(caminho.getParent());
                Files.write(caminho, arquivoImagem.getBytes());

                produto.setImagem(nomeArquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao salvar a imagem", e);
            }
        }

        Produto produtoSalvo = produtoRepository.save(produto);

        return converterParaDTO(produtoSalvo);
    }

    public List<ProdutoDTO> listarProdutosDoUsuarioLogado() {
        // Pega o email/username do usuário autenticado no token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Busca o usuário no banco pelo email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no token"));

        // Filtra os produtos
        return produtoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private ProdutoDTO converterParaDTO(Produto produto) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setCategoriaId(produto.getCategoria().getId());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setPrecoCusto(produto.getPrecoCusto());
        dto.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        dto.setStatus(produto.getStatus());
        dto.setLargura(produto.getLargura());
        dto.setAltura(produto.getAltura());
        dto.setObservacoes(produto.getObservacoes());
        dto.setImagem(produto.getImagem());

        if (produto.getCategoria() != null) {
            dto.setNomeCategoria(produto.getCategoria().getNome());
        }

        if (produto.getMateriais() != null) {
            List<String> nomesMateriais = produto.getMateriais().stream()
                    .map(Material::getNome)
                    .toList();
            dto.setMateriais(nomesMateriais);
        }

        return dto;
    }

    public ProdutoDTO atualizar(Long id, ProdutoDTO dto, MultipartFile arquivoImagem) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setPrecoCusto(dto.getPrecoCusto());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setStatus(dto.getStatus());
        produto.setLargura(dto.getLargura());
        produto.setAltura(dto.getAltura());
        produto.setObservacoes(dto.getObservacoes());
        if (arquivoImagem != null && !arquivoImagem.isEmpty()) {
            try {
                String nomeArquivo = UUID.randomUUID() + "_" + arquivoImagem.getOriginalFilename();
                Path caminho = Paths.get("uploads/" + nomeArquivo);
                Files.createDirectories(caminho.getParent());
                Files.write(caminho, arquivoImagem.getBytes());
                produto.setImagem(nomeArquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao salvar nova imagem", e);
            }
        }

        return converterParaDTO(produtoRepository.save(produto));
    }


    public ProdutoDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        return converterParaDTO(produto);
    }


}