package br.com.bb.banco.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClienteDadosDto(

    Long idCliente,

    String nome,
    
    String cpf,
    
    String email,
    
    String senha,

    String telefone,

    LocalDate dataDeNascimento,

    String cep,

    String estado,

    String cidade,

    String bairro,

    String rua,

    String numeroRua,

    String pcd,
    
    String sexo,
    
    String escolaridade,
    
    String estadoCivil,
    
    String ocupacao,
    
    Float rendaMensal  

) { }
