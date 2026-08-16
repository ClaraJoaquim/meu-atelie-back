package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tcc.meu_atelie.enums.StatusEncomenda;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FaturamentoMensalBrutoDTO {
    private LocalDate dataEntrega;
    private StatusEncomenda status;
    private BigDecimal valorTotal;

    public FaturamentoMensalBrutoDTO(LocalDate dataEntrega, StatusEncomenda status, BigDecimal valorTotal) {
        this.dataEntrega = dataEntrega;
        this.status = status;
        this.valorTotal = valorTotal;
    }
}
