package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.models.Material;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialDTO {
    private Long id;
    private String nome;
    private String categoria;
    private Integer quantidadeEstoque;
    private BigDecimal precoUnitario;
    private String unidadeMedida;
    private String anotacoes;
    private String fornecedor;
    private String codigoReferencia;
    private Integer estoqueMinimo;

    public MaterialDTO() {}

    public MaterialDTO(Material material) {
        this.id = material.getId();
        this.nome = material.getNome();
        this.quantidadeEstoque = material.getQuantidadeEstoque();
        this.precoUnitario = material.getPrecoUnitario();
        this.unidadeMedida = material.getUnidadeMedida();
        this.anotacoes = material.getAnotacoes();
        if (material.getCategoria() != null) {
            this.categoria = material.getCategoria();
        }
        if (material.getFornecedor() != null) {
            this.fornecedor = material.getFornecedor().getNome();
        }
        this.codigoReferencia = material.getCodigoReferencia();
        this.estoqueMinimo = material.getEstoqueMinimo();
    }
}
