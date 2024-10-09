package br.com.bb.banco.utils;

import org.springframework.stereotype.Component;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.entity.types.Escolaridade;
import br.com.bb.banco.entity.types.EstadoCivil;
import br.com.bb.banco.entity.types.Ocupacao;
import br.com.bb.banco.entity.types.PessoaComDeficiencia;
import br.com.bb.banco.entity.types.Sexo;

@Component
public class ConversorDeObjetos {
    

    public ClienteDados clienteDadosDtoParaEntity(ClienteDadosDto dto){
        
        ClienteDados entity = ClienteDados.builder()
        .idCliente(dto.idCliente())
        .nome(dto.nome())
        .cpf(dto.cpf())
        .email(dto.email())
        .senha(dto.senha())
        .telefone(dto.telefone())
        .dataDeNascimento(dto.dataDeNascimento())
        .cep(dto.cep())
        .estado(dto.estado())
        .cidade(dto.cidade())
        .bairro(dto.bairro())
        .rua(dto.rua())
        .numeroRua(dto.numeroRua())
        .pcd(dto.pcd() != null ? PessoaComDeficiencia.valueOf(dto.pcd()) : null)
        .sexo(Sexo.valueOf(dto.sexo()))
        .escolaridade(Escolaridade.valueOf(dto.escolaridade()))
        .estadoCivil(EstadoCivil.valueOf(dto.estadoCivil()))
        .ocupacao(Ocupacao.valueOf(dto.ocupacao()))
        .rendaMensal(dto.rendaMensal())
        .build();

        return entity;
    }


    public ClienteDadosDto clienteDadosEntityParaDto(ClienteDados entity){
        
        ClienteDadosDto dto = ClienteDadosDto.builder()
        .idCliente(entity.getIdCliente())
        .nome(entity.getNome())
        .cpf(entity.getCpf())
        .email(entity.getEmail())
        .senha(entity.getSenha())
        .telefone(entity.getTelefone())
        .dataDeNascimento(entity.getDataDeNascimento())
        .cep(entity.getCep())
        .estado(entity.getEstado())
        .cidade(entity.getCidade())
        .bairro(entity.getBairro())
        .rua(entity.getRua())
        .numeroRua(entity.getNumeroRua())
        .pcd(entity.getPcd() != null ? entity.getPcd().name() : null)
        .sexo(entity.getSexo().name())
        .escolaridade(entity.getEscolaridade().name())
        .estadoCivil(entity.getEstadoCivil().name())
        .ocupacao(entity.getOcupacao().name())
        .rendaMensal(entity.getRendaMensal())
        .build();

        return dto;
    }

    public ClientePerfilDto ClientePerfilEntityParaDto(ClientePerfil entity){

        ClientePerfilDto dto = ClientePerfilDto.builder()
        .idPerfil(entity.getIdPerfil())
        .score(entity.getScore())
        .notaDoPerfil(entity.getNotaDoPerfil())
        .avaliacao(entity.getAvaliacao().name())
        .idCliente(entity.getIdCliente().getIdCliente())
        .build();


        return dto;
    }

}
