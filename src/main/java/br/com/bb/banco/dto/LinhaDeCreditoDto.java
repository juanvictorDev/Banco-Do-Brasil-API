package br.com.bb.banco.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder @Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinhaDeCreditoDto{
        
    Long idLinhaDeCredito;
    
    String nome;

    String descricao;

    String imagemNome;

    String linkSite;

    String tipo;
    
    Float taxaDeJuros;

    Float valor;

    Float taxaDeJurosTotal;

    Float montante;

    Float valorDaParcela;

    Integer numeroDeParcelas;
    
}
