package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.Encomenda;

import java.time.LocalDate;
import java.util.List;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {
    List<Encomenda> findByUsuarioEmailOrderByIdAsc(String email);

    List<Encomenda> findByStatusAndDataPedidoBefore(StatusEncomenda status, LocalDate data);
}