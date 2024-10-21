package br.com.bb.banco.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bb.banco.dto.LinhaDeCreditoDto;
import br.com.bb.banco.service.LinhasDeCreditoService;


@RestController
public class LinhasDeCreditoController {
    
    @Autowired
    LinhasDeCreditoService linhasDeCreditoService;


    @GetMapping("/linhas-de-credito")
    public ResponseEntity<PagedModel<EntityModel<LinhaDeCreditoDto>>> buscarLinhasDeCredito(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhasDeCredito(page, size));
    }


    @GetMapping("/linhas-de-credito/{id}")
    public ResponseEntity<EntityModel<LinhaDeCreditoDto>> buscarLinhaDeCredito(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhaDeCredito(id));
    }
    

    @GetMapping("/linhas-de-credito/grupo/{tipo}")
    public ResponseEntity<CollectionModel<EntityModel<LinhaDeCreditoDto>>> buscarLinhasDeCreditoPorTipo(@PathVariable("tipo") String tipo) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhaDeCreditoPorTipo(tipo));
    }
    
}
