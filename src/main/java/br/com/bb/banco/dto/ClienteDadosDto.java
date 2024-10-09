package br.com.bb.banco.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
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
    
    Double rendaMensal  

) { }
