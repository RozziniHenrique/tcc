package com.tcc.uscs.repository;

import com.tcc.uscs.model.avaliacao.Avaliacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
  boolean existsByAgendamentoId(Long idAgendamento);
  Optional<Avaliacao> findByAgendamentoId(Long idAgendamento);
}
