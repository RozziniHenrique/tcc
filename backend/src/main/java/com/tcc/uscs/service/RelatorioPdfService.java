package com.tcc.uscs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelatorioPdfService {

  private static final PDType1Font FONTE_NORMAL = new PDType1Font(
    Standard14Fonts.FontName.HELVETICA
  );

  private static final PDType1Font FONTE_NEGRITO = new PDType1Font(
    Standard14Fonts.FontName.HELVETICA_BOLD
  );

  private final RelatorioService relatorioService;

  public byte[] gerarPdf(LocalDate inicio, LocalDate fim) {
    var relatorio = relatorioService.gerarRelatorioCompleto(inicio, fim);

    try (
      var documento = new PDDocument();
      var saida = new ByteArrayOutputStream()
    ) {
      var pagina = new PaginaPdf(documento);

      pagina.escrever("RELATÓRIO STFER", FONTE_NEGRITO, 16);
      pagina.escrever("Período: " + inicio + " até " + fim, FONTE_NORMAL, 11);
      pagina.pularLinha();

      pagina.escrever("RESUMO", FONTE_NEGRITO, 13);
      pagina.escrever(
        "Total de agendamentos: " + relatorio.resumo().totalAgendamentos(),
        FONTE_NORMAL,
        11
      );
      pagina.escrever(
        "Faturamento total: " +
          formatarMoeda(relatorio.resumo().faturamentoTotal()),
        FONTE_NORMAL,
        11
      );
      pagina.pularLinha();

      pagina.escrever("ALUNOS POR CURSO", FONTE_NEGRITO, 13);

      for (var item : relatorio.alunosPorCurso()) {
        pagina.escrever(
          item.idCurso() +
            " - " +
            item.nomeCurso() +
            ": " +
            item.quantidadeAlunos() +
            " aluno(s)",
          FONTE_NORMAL,
          10
        );
      }

      pagina.pularLinha();
      pagina.escrever(
        "AGENDAMENTOS E FATURAMENTO POR CURSO",
        FONTE_NEGRITO,
        13
      );

      for (var item : relatorio.agendamentosPorCurso()) {
        pagina.escrever(
          item.idCurso() +
            " - " +
            item.nomeCurso() +
            ": " +
            item.quantidadeAgendamentos() +
            " agendamento(s) | " +
            formatarMoeda(item.faturamentoTotal()),
          FONTE_NORMAL,
          10
        );
      }

      pagina.fechar();
      documento.save(saida);
      return saida.toByteArray();
    } catch (IOException ex) {
      throw new IllegalStateException("Não foi possível gerar o PDF.", ex);
    }
  }

  private String formatarMoeda(BigDecimal valor) {
    var formato = NumberFormat.getCurrencyInstance(
      Locale.forLanguageTag("pt-BR")
    );

    return formato.format(valor == null ? BigDecimal.ZERO : valor);
  }

  private static class PaginaPdf {

    private static final float MARGEM = 50;
    private static final float LIMITE_INFERIOR = 50;
    private static final float ESPACAMENTO = 18;

    private final PDDocument documento;
    private PDPageContentStream conteudo;
    private float posicaoY;

    PaginaPdf(PDDocument documento) throws IOException {
      this.documento = documento;
      novaPagina();
    }

    void escrever(String texto, PDType1Font fonte, float tamanho)
      throws IOException {
      if (posicaoY < LIMITE_INFERIOR) {
        novaPagina();
      }

      conteudo.beginText();
      conteudo.setFont(fonte, tamanho);
      conteudo.newLineAtOffset(MARGEM, posicaoY);
      conteudo.showText(limitar(texto));
      conteudo.endText();

      posicaoY -= ESPACAMENTO;
    }

    void pularLinha() {
      posicaoY -= ESPACAMENTO / 2;
    }

    void novaPagina() throws IOException {
      if (conteudo != null) {
        conteudo.close();
      }

      var pagina = new PDPage(PDRectangle.A4);
      documento.addPage(pagina);
      conteudo = new PDPageContentStream(documento, pagina);
      posicaoY = pagina.getMediaBox().getHeight() - MARGEM;
    }

    void fechar() throws IOException {
      if (conteudo != null) {
        conteudo.close();
      }
    }

    private String limitar(String texto) {
      var textoSeguro =
        texto == null ? "" : texto.replace("\n", " ").replace("\r", " ");

      return textoSeguro.length() > 100
        ? textoSeguro.substring(0, 97) + "..."
        : textoSeguro;
    }
  }
}
