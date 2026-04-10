package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.ClienteDTO;
import tcc.meu_atelie.dto.ClienteResumoDTO;
import tcc.meu_atelie.models.CanalAquisicao;
import tcc.meu_atelie.models.Cliente;
import tcc.meu_atelie.repositories.CanalAquisicaoRepository;
import tcc.meu_atelie.repositories.ClienteRepository;
import tcc.meu_atelie.services.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CanalAquisicaoRepository canalAquisicaoRepository;

    @Autowired
    private ClienteRepository clienteRepository;


    @PostMapping
    public Cliente salvarCliente(@RequestBody ClienteDTO dto) {
        return clienteService.salvarCliente(dto);
    }

    @GetMapping("/canal-aquisicao")
    public List<CanalAquisicao> listarCanais() {
        return clienteService.listarCanais();
    }

    @GetMapping("/resumo")
    public ResponseEntity<List<ClienteResumoDTO>> listarResumo() {
        return ResponseEntity.ok(clienteService.listarResumoClientes());
    }

}