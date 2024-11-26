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

    /**
     * [TODAS AS LINHAS DE CREDITO]
     * Retorna todas as linhas de crédito de forma paginada
     * @param page número da página desejada (começa em 0)
     * @param size quantidade de itens por página
     * @return ResponseEntity contendo PagedModel com as linhas de crédito
     */
    @GetMapping("/linhas-de-credito")
    public ResponseEntity<PagedModel<EntityModel<LinhaDeCreditoDto>>> buscarLinhasDeCredito(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhasDeCredito(page, size));
    }
    
    /**
     * [LINHA DE CREDITO ESPECIFICA]
     * Busca uma linha de crédito específica pelo ID
     * @param id identificador único da linha de crédito
     * @return ResponseEntity contendo EntityModel com a linha de crédito encontrada
     */
    @GetMapping("/linhas-de-credito/{id}")
    public ResponseEntity<EntityModel<LinhaDeCreditoDto>> buscarLinhaDeCredito(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(linhasDeCreditoService.encontrarLinhaDeCredito(id));
    }
    
    /**
     * [LINHAS DE CREDITO POR TIPO]
     * Realiza simulação de uma linha de crédito específica, valores são opcionais pois ele trata varias
     * linhas de creditos, e cada uma possui um tipo diferente de logica e parametros de entrada
     * @param tipo tipo da linha de crédito a ser simulada
     * @param valor valor desejado para simulação (opcional)
     * @param parcelas número de parcelas para simulação (opcional)
     * @param custo custo do bem para simulação (opcional)
     * @param data data para simulação (opcional)
     * @return ResponseEntity contendo EntityModel com o resultado da simulação
     */
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
