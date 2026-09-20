package com.tcc.uscs.model.relatorio.dto;

public record DesempenhoAlunoRelatorioDTO(
  Long idAluno,
  String nomeAluno,
  Long idCurso,
  String nomeCurso,
  Long totalAgendamentos,
  Long totalConcluidos,
  Long totalCancelados,
  Double mediaAvaliacoes,
  Long quantidadeAvaliacoes
) {
  public DesempenhoAlunoRelatorioDTO {
    mediaAvaliacoes = mediaAvaliacoes == null ? 0.0 : mediaAvaliacoes;
  }
}
