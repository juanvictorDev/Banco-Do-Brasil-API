package br.com.bb.banco.dto;

import lombok.Builder;

@Builder
public record ClienteContaDto(

    Long idConta,

    String agencia,

    String numeroDaConta,

    Double saldo 

) { }
