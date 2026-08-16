package tcc.meu_atelie.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.DashboardDTO;
import tcc.meu_atelie.dto.EncomendaDTO;
import tcc.meu_atelie.dto.ItemDTO;
import tcc.meu_atelie.dto.PagamentoDTO;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.*;
import tcc.meu_atelie.repositories.*;
import tcc.meu_atelie.dto.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EncomendaService {

    private final EncomendaRepository encomendaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final MaterialRepository materialRepository;

    @Transactional
    public EncomendaDTO cadastrar(EncomendaDTO dto) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado).orElseThrow();

        Cliente cliente = clienteRepository.findByIdAndUsuarioEmailAndAtivoTrue(dto.getClienteId(), emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado ou inativo"));

        Encomenda encomenda = new Encomenda();
        encomenda.setCliente(cliente);
        encomenda.setUsuario(usuario);
        encomenda.setDataPedido(dto.getDataPedido() != null ? dto.getDataPedido() : LocalDate.now());
        encomenda.setDataEntrega(dto.getDataEntrega());
        encomenda.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusEncomenda.EM_ANDAMENTO);

        if (encomenda.getDataEntrega() != null && encomenda.getDataEntrega().isBefore(encomenda.getDataPedido())) {
            throw new IllegalArgumentException("A data de entrega não pode ser anterior à data do pedido.");
        }

        List<ItemEncomenda> itens = dto.getItens().stream().map(itemDto -> {
            ItemEncomenda item = new ItemEncomenda();
            Produto produto = produtoRepository.findById(itemDto.getProdutoId()).orElseThrow();

            if (!produto.getUsuario().getEmail().equals(emailLogado)) {
                throw new RuntimeException("Produto inválido para este usuário");
            }

            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setValorUnitario(produto.getPreco());
            item.setEncomenda(encomenda);
            return item;
        }).toList();
        encomenda.setItens(itens);

        Pagamento pagamento = new Pagamento();
        pagamento.setFrete(dto.getPagamento().getFrete() != null ? dto.getPagamento().getFrete() : BigDecimal.ZERO);
        pagamento.setDescontoValor(dto.getPagamento().getDescontoValor() != null ? dto.getPagamento().getDescontoValor() : BigDecimal.ZERO);
        pagamento.setCondicoesPagamento(dto.getPagamento().getCondicoesPagamento());
        pagamento.setEncomenda(encomenda);
        encomenda.setPagamento(pagamento);

        BigDecimal totalItens = itens.stream()
                .map(i -> i.getValorUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        encomenda.setValorTotal(totalItens.add(pagamento.getFrete()).subtract(pagamento.getDescontoValor()));

        Encomenda salva = encomendaRepository.save(encomenda);
        return buscarPorId(salva.getId());
    }

    public List<EncomendaDTO> listarTodas() {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        return encomendaRepository.findByUsuarioEmailOrderByIdAsc(emailLogado).stream().map(encomenda -> {
            EncomendaDTO dto = new EncomendaDTO();
            dto.setId(encomenda.getId());
            dto.setNomeCliente(encomenda.getCliente().getNome());
            dto.setDataPedido(encomenda.getDataPedido());
            dto.setDataEntrega(encomenda.getDataEntrega());

            dto.setStatus(encomenda.getStatus());
            dto.setValorTotal(encomenda.getValorTotal());
            return dto;
        }).toList();
    }

    public EncomendaDTO buscarPorId(Long id) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Encomenda encomenda = encomendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encomenda não encontrada"));

        if (!encomenda.getUsuario().getEmail().equals(emailLogado)) {
            throw new RuntimeException("Acesso negado");
        }

        EncomendaDTO dto = new EncomendaDTO();
        dto.setId(encomenda.getId());
        dto.setClienteId(encomenda.getCliente().getId());
        dto.setNomeCliente(encomenda.getCliente().getNome());
        dto.setDataPedido(encomenda.getDataPedido());
        dto.setDataEntrega(encomenda.getDataEntrega());
        dto.setStatus(encomenda.getStatus());
        dto.setValorTotal(encomenda.getValorTotal());

        if (encomenda.getItens() != null) {
            dto.setItens(encomenda.getItens().stream().map(item -> {
                ItemDTO itemDto = new ItemDTO();
                itemDto.setProdutoId(item.getProduto().getId());
                itemDto.setQuantidade(item.getQuantidade());
                return itemDto;
            }).toList());
        }

        if (encomenda.getPagamento() != null) {
            PagamentoDTO pagDto = new PagamentoDTO();
            pagDto.setFrete(encomenda.getPagamento().getFrete());
            pagDto.setDescontoValor(encomenda.getPagamento().getDescontoValor());
            pagDto.setCondicoesPagamento(encomenda.getPagamento().getCondicoesPagamento());
            dto.setPagamento(pagDto);
        }

        return dto;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cancelarOrcamentosVencidos() {
        LocalDate dataLimite = LocalDate.now().minusDays(30);

        List<Encomenda> orcamentosVencidos = encomendaRepository
                .findByStatusAndDataPedidoBefore(StatusEncomenda.ORCAMENTO, dataLimite);

        if (!orcamentosVencidos.isEmpty()) {
            orcamentosVencidos.forEach(enc -> enc.setStatus(StatusEncomenda.CANCELADO));
            encomendaRepository.saveAll(orcamentosVencidos);
        }
    }

    @Transactional
    public EncomendaDTO atualizar(Long id, EncomendaDTO dto) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        // Busca a encomenda e valida o dono
        Encomenda encomenda = encomendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encomenda não encontrada"));

        if (!encomenda.getUsuario().getEmail().equals(emailLogado)) {
            throw new RuntimeException("Acesso negado");
        }

        // Valida e atualiza o cliente
        Cliente cliente = clienteRepository.findByIdAndUsuarioEmailAndAtivoTrue(dto.getClienteId(), emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado ou inativo"));
        encomenda.setCliente(cliente);

        // Atualiza dados básicos
        encomenda.setDataPedido(dto.getDataPedido() != null ? dto.getDataPedido() : encomenda.getDataPedido());
        encomenda.setDataEntrega(dto.getDataEntrega());
        encomenda.setStatus(dto.getStatus() != null ? dto.getStatus() : encomenda.getStatus());

        if (encomenda.getDataEntrega() != null && encomenda.getDataEntrega().isBefore(encomenda.getDataPedido())) {
            throw new IllegalArgumentException("A data de entrega não pode ser anterior à data do pedido.");
        }

        // Atualiza os Itens (Limpa os antigos e adiciona os novos)
        encomenda.getItens().clear();
        List<ItemEncomenda> novosItens = dto.getItens().stream().map(itemDto -> {
            Produto produto = produtoRepository.findById(itemDto.getProdutoId()).orElseThrow();
            if (!produto.getUsuario().getEmail().equals(emailLogado)) {
                throw new RuntimeException("Produto inválido para este usuário");
            }
            ItemEncomenda item = new ItemEncomenda();
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setValorUnitario(produto.getPreco());
            item.setEncomenda(encomenda);
            return item;
        }).toList();
        encomenda.getItens().addAll(novosItens);

        // Atualiza Pagamento
        Pagamento pagamento = encomenda.getPagamento();
        pagamento.setFrete(dto.getPagamento().getFrete() != null ? dto.getPagamento().getFrete() : BigDecimal.ZERO);
        pagamento.setDescontoValor(dto.getPagamento().getDescontoValor() != null ? dto.getPagamento().getDescontoValor() : BigDecimal.ZERO);
        pagamento.setCondicoesPagamento(dto.getPagamento().getCondicoesPagamento());

        // Recalcula Valor Total
        BigDecimal totalItens = novosItens.stream()
                .map(i -> i.getValorUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        encomenda.setValorTotal(totalItens.add(pagamento.getFrete()).subtract(pagamento.getDescontoValor()));

        Encomenda salva = encomendaRepository.save(encomenda);
        return buscarPorId(salva.getId());
    }

    public DashboardDTO obterDadosDashboard() {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + emailLogado));

        List<Encomenda> encomendas = encomendaRepository.findByUsuarioEmailOrderByIdAsc(emailLogado);
        List<Material> materiais = materialRepository.findAll();

        LocalDate hoje = LocalDate.now();
        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setNomeUsuario(usuario.getNome().split(" ")[0]);

        // 1. Resumo
        ResumoDTO resumo = new ResumoDTO();
        resumo.setEmAndamento(encomendas.stream().filter(e -> e.getStatus() == StatusEncomenda.EM_ANDAMENTO).count());
        resumo.setFinalizados(encomendas.stream().filter(e -> e.getStatus() == StatusEncomenda.FINALIZADO).count());
        resumo.setAtrasados(encomendas.stream().filter(e -> e.getDataEntrega() != null && e.getDataEntrega().isBefore(hoje) && e.getStatus() != StatusEncomenda.FINALIZADO && e.getStatus() != StatusEncomenda.CANCELADO).count());
        dashboard.setResumo(resumo);

        // 2. Alertas
        List<AlertaDTO> alertas = new ArrayList<>();

        // Alertas de Encomendas
        encomendas.stream()
                .filter(e -> e.getDataEntrega() != null && e.getStatus() != StatusEncomenda.FINALIZADO && e.getStatus() != StatusEncomenda.CANCELADO)
                .forEach(e -> {
                    long dias = ChronoUnit.DAYS.between(hoje, e.getDataEntrega());
                    if (dias < 0) {
                        AlertaDTO alerta = new AlertaDTO();
                        alerta.setTipo("danger");
                        alerta.setTitulo("Encomenda atrasada");
                        alerta.setMensagem(String.format("Pedido #%03d  de %s está %d dias atrasado.", e.getId(), e.getCliente().getNome(), Math.abs(dias)));
                        alertas.add(alerta);
                    } else if (dias <= 2) {
                        AlertaDTO alerta = new AlertaDTO();
                        alerta.setTipo("warning");
                        alerta.setTitulo("Prazo próximo");
                        alerta.setMensagem(String.format("Pedido #%03d  de %s vence em %d dias.", e.getId(), e.getCliente().getNome(), dias));
                        alertas.add(alerta);
                    }
                });

        // Alertas de Estoque (Atualizado para o model Material)
        materiais.stream()
                .filter(m -> m.getUsuario().getEmail().equals(emailLogado))
                .filter(m -> m.getEstoqueMinimo() != null && m.getQuantidadeEstoque() <= m.getEstoqueMinimo())
                .forEach(m -> {
                    AlertaDTO alerta = new AlertaDTO();
                    alerta.setTipo("info");
                    alerta.setTitulo("Estoque Baixo");
                    alerta.setMensagem(String.format("O material '%s' está abaixo do mínimo (Restam %d %s).", m.getNome(), m.getQuantidadeEstoque(), m.getUnidadeMedida()));
                    alertas.add(alerta);
                });

        dashboard.setAlertas(alertas);

        // 3. Pedidos Recentes
        List<PedidoRecenteDTO> recentes = encomendas.stream()
                .sorted(Comparator.comparing(Encomenda::getId).reversed())
                .limit(5)
                .map(e -> {
                    PedidoRecenteDTO dto = new PedidoRecenteDTO();
                    dto.setId(e.getId());
                    dto.setNomeCliente(e.getCliente().getNome());
                    dto.setTipoPrincipal(e.getItens().isEmpty() ? "Diversos" : e.getItens().get(0).getProduto().getNome());
                    dto.setDataEntrega(e.getDataEntrega());
                    dto.setStatus(e.getStatus());
                    return dto;
                }).toList();
        dashboard.setPedidosRecentes(recentes);

        return dashboard;
    }
}
