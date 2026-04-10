package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ClienteResumoDTO {
    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private Long totalPedidos;
    private BigDecimal totalGasto;
    private LocalDate dataCadastro;

    // Construtor customizado para tratar o valor nulo do banco
    public ClienteResumoDTO(Long id, String nome, String telefone, String email, Long totalPedidos, BigDecimal totalGasto, LocalDate dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.totalPedidos = totalPedidos;
        this.totalGasto = totalGasto != null ? totalGasto : BigDecimal.ZERO; // Troca null por 0
        this.dataCadastro = dataCadastro;
    }
}