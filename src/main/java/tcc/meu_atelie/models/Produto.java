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
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "categoria_id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "usuario_id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private BigDecimal preco;

    private String imagem;

    @OneToMany(mappedBy = "produto")
    @JsonIgnore
    private List<ItemEncomenda> itens;

    @ManyToMany
    @JoinTable(
            name = "produto_material",
            joinColumns = @JoinColumn(name = "produto_id_produto"),
            inverseJoinColumns = @JoinColumn(name = "material_id_material")
    )
    private List<Material> materiais;
}