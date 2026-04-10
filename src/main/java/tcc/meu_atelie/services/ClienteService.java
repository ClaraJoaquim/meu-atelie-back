package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.ClienteDTO;
import tcc.meu_atelie.dto.ClienteResumoDTO;
import tcc.meu_atelie.models.CanalAquisicao;
import tcc.meu_atelie.models.Cliente;
import tcc.meu_atelie.models.Endereco;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.CanalAquisicaoRepository;
import tcc.meu_atelie.repositories.ClienteRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CanalAquisicaoRepository canalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Cliente salvarCliente(ClienteDTO dto) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        CanalAquisicao canal = canalRepository.findById(dto.getCanalId())
                .orElseThrow(() -> new RuntimeException("Canal não encontrado"));

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setCnpj(dto.getCnpj());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());
        cliente.setObservacoes(dto.getObservacoes());
        cliente.setCanalAquisicao(canal);
        cliente.setUsuario(usuarioLogado);

        if (dto.getEndereco() != null) {

            Endereco endereco = new Endereco();
            endereco.setCep(dto.getEndereco().getCep());
            endereco.setRua(dto.getEndereco().getRua());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setComplemento(dto.getEndereco().getComplemento());
            endereco.setBairro(dto.getEndereco().getBairro());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setEstado(dto.getEndereco().getEstado());

            endereco.setCliente(cliente);
            cliente.setEndereco(endereco);
        }

        return clienteRepository.save(cliente);
    }

    public List<CanalAquisicao> listarCanais() {
        return canalRepository.findAll();
    }

    public List<ClienteResumoDTO> listarResumoClientes() {
        return clienteRepository.buscarResumoClientes();
    }
}
