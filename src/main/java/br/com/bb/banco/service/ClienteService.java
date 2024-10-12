package br.com.bb.banco.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.stereotype.Service;
import br.com.bb.banco.cliente.CriarPerfil;
import br.com.bb.banco.controller.ClienteController;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.entity.ClienteConta;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.repository.ClienteDadosRepository;
import br.com.bb.banco.repository.ClientePerfilRepository;
import br.com.bb.banco.utils.ConversorDeObjetos;


@Service
public class ClienteService {
    
    @Autowired
    ClienteDadosRepository clienteDadosRepository;

    @Autowired
    ClientePerfilRepository clientePerfilRepository;

    @Autowired
    ConversorDeObjetos conversorDeObjetos;

    @Autowired
    CriarPerfil criarPerfil;


    public EntityModel<ClienteDadosDto> salvarCliente(ClienteDadosDto requestBody){
        ClienteDados clienteDados = conversorDeObjetos.clienteDadosDtoParaEntity(requestBody);
        ClienteConta clienteConta = new ClienteConta();

        clienteDados.setClienteConta(clienteConta);
        clienteConta.setClienteDados(clienteDados);
    
        ClienteDados clienteDadosDatabase = clienteDadosRepository.save(clienteDados);

        criarPerfil.criar(clienteDadosDatabase);
        
        Link selfLink = linkTo(methodOn(ClienteController.class).buscarCliente(clienteDadosDatabase.getIdCliente())).withSelfRel();
        Link perfilLink = linkTo(methodOn(ClienteController.class).buscarPerfil(clienteDadosDatabase.getIdCliente())).withRel("perfil");
        EntityModel<ClienteDadosDto> entityModel = EntityModel.of(conversorDeObjetos.clienteDadosEntityParaDto(clienteDadosDatabase), selfLink, perfilLink);

        return entityModel;
    }


    public EntityModel<ClienteDadosDto> encontrarCliente(Long id){
        ClienteDados clienteDados = clienteDadosRepository.findById(id).get();
        clienteDados.setSenha(null);

        Link selfLink = linkTo(methodOn(ClienteController.class).buscarCliente(clienteDados.getIdCliente())).withSelfRel();
        Link perfilLink = linkTo(methodOn(ClienteController.class).buscarPerfil(clienteDados.getIdCliente())).withRel("perfil");
        EntityModel<ClienteDadosDto> entityModel = EntityModel.of(conversorDeObjetos.clienteDadosEntityParaDto(clienteDados), selfLink, perfilLink);

        return entityModel;
    }


    public EntityModel<ClientePerfilDto> encontrarPerfil(Long id){
        ClientePerfil clientePerfil = clientePerfilRepository.findByIdCliente(id).get();

        Link selfLink = linkTo(methodOn(ClienteController.class).buscarPerfil(id)).withSelfRel();
        EntityModel<ClientePerfilDto> entityModel = EntityModel.of(conversorDeObjetos.ClientePerfilEntityParaDto(clientePerfil), selfLink);

        return entityModel;
    }

}
