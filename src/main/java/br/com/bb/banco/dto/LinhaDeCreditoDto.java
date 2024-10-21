package br.com.bb.banco.dto;

import lombok.Builder;

@Builder
public record LinhaDeCreditoDto(
    
    Long idLinhaDeCredito,
    
    String nome,

    String descricao,

    String imagemNome,

    String linkSite,

    String tipo
    
) {}
