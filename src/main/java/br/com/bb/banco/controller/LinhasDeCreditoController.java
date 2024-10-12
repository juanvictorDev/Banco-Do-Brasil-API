package br.com.bb.banco.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.service.LinhasDeCreditoService;


@RestController
public class LinhasDeCreditoController {
    
    @Autowired
    LinhasDeCreditoService linhasDeCreditoService;


    @GetMapping("/linhas-de-credito")
    public ResponseEntity<CollectionModel<EntityModel<LinhaDeCredito>>> buscarLinhasDeCredito() {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhasDeCredito());
    }

}
