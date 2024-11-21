package br.com.bb.banco.controller;

import java.time.LocalDate;
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
    
    LinhasDeCreditoService linhasDeCreditoService;

    public LinhasDeCreditoController(LinhasDeCreditoService linhasDeCreditoService) {
        this.linhasDeCreditoService = linhasDeCreditoService;
    }

    
    // -- {LINHAS DE CREDITO} --

    // [TODAS AS LINHAS DE CREDITO]
    // Retornar todas as linhas de forma paginada, recebe page e size opcionais
    @GetMapping("/linhas-de-credito")
    public ResponseEntity<PagedModel<EntityModel<LinhaDeCreditoDto>>> buscarLinhasDeCredito(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhasDeCredito(page, size));
    }
    
    // [LINHA DE CREDITO ESPECIFICA]
    // Retorna uma linha de credito especifica, recebe id
    @GetMapping("/linhas-de-credito/{id}")
    public ResponseEntity<EntityModel<LinhaDeCreditoDto>> buscarLinhaDeCredito(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhaDeCredito(id));
    }
    
    // [SIMULAR LINHA DE CREDITO]
    // Retorna a simulação de valores de uma linha especifica, rebecebe parametros opcionais lida com varios tipos de linhas
    @GetMapping("/linhas-de-credito/simular/{tipo}")
    public ResponseEntity<EntityModel<LinhaDeCreditoDto>> simularLinhasDeCreditoPorTipo(
        @PathVariable("tipo") String tipo,
        @RequestParam(name = "valor", required = false) Float valor,
        @RequestParam(name = "parcelas", required = false) Integer parcelas,
        @RequestParam(name = "custo", required = false) Float custo,
        @RequestParam(name = "data", required = false) LocalDate data
    ) {
        return ResponseEntity.ok().body(linhasDeCreditoService.simularLinhaDeCreditoPorTipo(tipo, valor, parcelas, custo, data));
    }
    
}
