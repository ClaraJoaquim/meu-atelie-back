package tcc.meu_atelie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "qtd_total")
    private Integer qtdTotal;

    private BigDecimal preco;

    @ManyToMany(mappedBy = "materiais")
    @JsonIgnore
    private List<Produto> produtos;
}