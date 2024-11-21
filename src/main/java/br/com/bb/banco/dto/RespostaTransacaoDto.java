package br.com.bb.banco.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespostaTransacaoDto(

    String agencia,

    String conta,

    Double valorDepositado,

    Double valorSacado,

    Double valorTransferido,

    Double saldoAnterior,

    Double saldoAtual,

    String agenciaDestinatario,

    String contaDestinatario

) { }
