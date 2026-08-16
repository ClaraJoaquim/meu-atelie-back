package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.FaturamentoMensalDTO;
import tcc.meu_atelie.dto.IndicadoresPeriodoDTO;
import tcc.meu_atelie.dto.PeriodoTipo;
import tcc.meu_atelie.dto.RankingClienteRelatorioDTO;
import tcc.meu_atelie.dto.RankingProdutoDTO;
import tcc.meu_atelie.services.RelatorioService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/indicadores")
    public ResponseEntity<IndicadoresPeriodoDTO> buscarIndicadores(
            @RequestParam PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(relatorioService.buscarIndicadores(periodo, data));
    }

    @GetMapping("/faturamento-mensal")
    public ResponseEntity<List<FaturamentoMensalDTO>> buscarFaturamentoMensal(
            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(relatorioService.buscarFaturamentoMensal(ano));
    }

    @GetMapping("/ranking-produtos")
    public ResponseEntity<List<RankingProdutoDTO>> buscarRankingProdutos(
            @RequestParam PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(relatorioService.buscarRankingProdutos(periodo, data));
    }

    @GetMapping("/ranking-clientes")
    public ResponseEntity<List<RankingClienteRelatorioDTO>> buscarRankingClientes(
            @RequestParam PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(relatorioService.buscarRankingClientes(periodo, data, limite));
    }
}
