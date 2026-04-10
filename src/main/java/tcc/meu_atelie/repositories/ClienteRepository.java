package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tcc.meu_atelie.dto.ClienteResumoDTO;
import tcc.meu_atelie.models.Cliente;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT new tcc.meu_atelie.dto.ClienteResumoDTO(" +
            "c.id, c.nome, c.telefone, c.email, " +
            "COUNT(e.id), SUM(e.valorTotal), c.dataCadastro) " +
            "FROM Cliente c LEFT JOIN Encomenda e ON e.cliente.id = c.id " +
            "GROUP BY c.id, c.nome, c.telefone, c.email, c.dataCadastro")
    List<ClienteResumoDTO> buscarResumoClientes();
}