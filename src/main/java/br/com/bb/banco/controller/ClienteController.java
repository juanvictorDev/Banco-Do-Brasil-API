package br.com.bb.banco.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.service.ClienteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class ClienteController {

    @Autowired
    ClienteService clienteService;


    @PostMapping("/cliente") 
    public ResponseEntity<EntityModel<ClienteDadosDto>> cadastrarCliente(@RequestBody ClienteDadosDto requestBody){
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.salvarCliente(requestBody));
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<EntityModel<ClienteDadosDto>> buscarCliente(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarCliente(id));
    }
    
    @GetMapping("cliente/{id}/perfil")
    public ResponseEntity<EntityModel<ClientePerfilDto>> buscarPerfil(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarPerfil(id));
    }
    
}
