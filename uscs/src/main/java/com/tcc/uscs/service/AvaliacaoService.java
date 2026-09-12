package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.avaliacao.Avaliacao;
import com.tcc.uscs.model.avaliacao.dto.AvaliacaoPendenteDTO;
import com.tcc.uscs.model.avaliacao.dto.CadastrarAvaliacaoDTO;
import com.tcc.uscs.model.avaliacao.dto.DetalharAvaliacaoDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AgendamentoRepository;
import com.tcc.uscs.repository.AvaliacaoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

  private final AvaliacaoRepository avaliacaoRepository;
  private final AgendamentoRepository agendamentoRepository;

  @Transactional
  public DetalharAvaliacaoDTO avaliar(CadastrarAvaliacaoDTO dados) {
    var agendamento = agendamentoRepository
      .findById(dados.idAgendamento())
      .orElseThrow(() -> new ValidacaoException("Agendamento não encontrado."));

    var usuarioLogado = usuarioAtual();
    if (!agendamento.getCliente().getId().equals(usuarioLogado.getId())) {
      throw new AccessDeniedException(
        "Você só pode avaliar os seus próprios agendamentos."
      );
    }

    if (
      agendamento.getStatus() != StatusAgendamento.CONCLUIDO ||
      !Boolean.TRUE.equals(agendamento.getAtivo())
    ) {
      throw new ValidacaoException(
        "Somente agendamentos concluídos podem ser avaliados."
      );
    }

    if (avaliacaoRepository.existsByAgendamentoId(dados.idAgendamento())) {
      throw new ValidacaoException(
        "Este agendamento já foi avaliado anteriormente."
      );
    }

    var avaliacao = new Avaliacao(
      agendamento,
      dados.nota(),
      dados.comentario()
    );
    avaliacaoRepository.save(avaliacao);

    return new DetalharAvaliacaoDTO(avaliacao);
  }

  @Transactional(readOnly = true)
  public DetalharAvaliacaoDTO buscarPorAgendamento(Long idAgendamento) {
    var avaliacao = avaliacaoRepository
      .findByAgendamentoId(idAgendamento)
      .orElseThrow(() ->
        new ValidacaoException(
          "Avaliação não encontrada para este agendamento."
        )
      );

    validarPermissaoDeConsulta(avaliacao, usuarioAtual());

    return new DetalharAvaliacaoDTO(avaliacao);
  }

  @Transactional(readOnly = true)
  public List<AvaliacaoPendenteDTO> listarPendentes() {
    var usuario = usuarioAtual();

    if (!usuario.possuiPerfil(TipoUsuario.CLIENTE)) {
      throw new AccessDeniedException(
        "Somente clientes podem consultar avaliações pendentes."
      );
    }

    return agendamentoRepository.listarPendentesDeAvaliacao(usuario.getId());
  }

  private Usuario usuarioAtual() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (
      authentication == null ||
      !(authentication.getPrincipal() instanceof Usuario usuario)
    ) {
      throw new AccessDeniedException("Usuário autenticado não encontrado.");
    }

    return usuario;
  }

  private void validarPermissaoDeConsulta(
    Avaliacao avaliacao,
    Usuario usuario
  ) {
    var agendamento = avaliacao.getAgendamento();
    var idUsuario = usuario.getId();

    var proprioCliente =
      agendamento.getCliente() != null &&
      agendamento.getCliente().getId().equals(idUsuario);

    var alunoResponsavel =
      agendamento.getAluno() != null &&
      agendamento.getAluno().getId().equals(idUsuario);

    var funcionario = usuario.possuiPerfil(TipoUsuario.FUNCIONARIO);

    if (!proprioCliente && !alunoResponsavel && !funcionario) {
      throw new AccessDeniedException(
        "Você não tem permissão para consultar esta avaliação."
      );
    }
  }
}
