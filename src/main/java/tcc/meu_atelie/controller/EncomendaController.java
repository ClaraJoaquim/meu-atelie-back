package tcc.meu_atelie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.DashboardDTO;
import tcc.meu_atelie.dto.EncomendaDTO;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.Encomenda;
import tcc.meu_atelie.services.EncomendaService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/encomendas")
@RequiredArgsConstructor
public class EncomendaController {

    private final EncomendaService encomendaService;

    @PostMapping
    public ResponseEntity<Long> cadastrarEncomenda(@RequestBody EncomendaDTO dto) {
        EncomendaDTO encomendaSalva = encomendaService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(encomendaSalva.getId());
    }

    @GetMapping
    public ResponseEntity<List<EncomendaDTO>> listarTodas() {
        return ResponseEntity.ok(encomendaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncomendaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(encomendaService.buscarPorId(id));
    }

    @GetMapping("/status")
    public List<StatusEncomenda> listarStatus() {
        return Arrays.asList(StatusEncomenda.values());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EncomendaDTO> atualizarEncomenda(@PathVariable Long id, @RequestBody EncomendaDTO dto) {
        EncomendaDTO encomendaAtualizada = encomendaService.atualizar(id, dto);
        return ResponseEntity.ok(encomendaAtualizada);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> obterDashboard() {
        return ResponseEntity.ok(encomendaService.obterDadosDashboard());
    }
}