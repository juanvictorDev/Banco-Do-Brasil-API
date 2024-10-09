package br.com.bb.banco.dto;

import lombok.Builder;

@Builder
public record ClientePerfilDto(
    
    Long idPerfil,
    
    Double notaDoPerfil,
    
    Double score,

    String avaliacao,

    Long idCliente
    
) {}
