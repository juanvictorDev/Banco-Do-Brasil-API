package br.com.bb.banco.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import br.com.bb.banco.controller.LinhasDeCreditoController;
import br.com.bb.banco.dto.LinhaDeCreditoDto;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.entity.types.TipoLinhaDeCredito;
import br.com.bb.banco.repository.LinhaDeCreditoRepository;
import br.com.bb.banco.utils.ConversorDeObjetos;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@Service
public class LinhasDeCreditoService {
    
    @Autowired
    LinhaDeCreditoRepository linhaDeCreditoRepository;

    @Autowired
    ConversorDeObjetos conversorDeObjetos;


    // Metodo para retornar todas as linhas de credito existentes de forma paginada
    public PagedModel<EntityModel<LinhaDeCreditoDto>> encontrarLinhasDeCredito(Integer page, Integer size){

        if (page == null || size == null) {
            throw new NullPointerException("Parâmetros de paginação não podem ser nulos");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<LinhaDeCredito> paginaDeLinhaDeCredito = linhaDeCreditoRepository.findAll(pageable);

        List<EntityModel<LinhaDeCreditoDto>> entityModelList = paginaDeLinhaDeCredito.getContent().stream()
        .map(linhaDeCredito -> {
            LinhaDeCreditoDto dto = conversorDeObjetos.LinhaDeCreditoEntityParaDto(linhaDeCredito);
            Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCredito.getIdLinhaDeCredito())).withSelfRel().withType("GET");
            return EntityModel.of(dto, selfLink);
        }).collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            pageable.getPageSize(),
            paginaDeLinhaDeCredito.getNumber(),
            paginaDeLinhaDeCredito.getTotalElements()
        );

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page, size)).withSelfRel().withType("GET"));
        
        if (!paginaDeLinhaDeCredito.isLast()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page + 1, size)).withRel("next").withType("GET"));
        }
        
        if (!paginaDeLinhaDeCredito.isFirst() && paginaDeLinhaDeCredito.hasContent()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page - 1, size)).withRel("previous").withType("GET"));
        }
        
        if (paginaDeLinhaDeCredito.hasPrevious()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, size)).withRel("firstPage").withType("GET"));
        }
        
        if (paginaDeLinhaDeCredito.hasNext() || !paginaDeLinhaDeCredito.hasContent()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(paginaDeLinhaDeCredito.getTotalPages() - 1, size)).withRel("lastPage").withType("GET"));
        }

        return PagedModel.of(entityModelList, pageMetadata, links);
    }


    // Metodo para encontrar linha de credito pelo id especifico
    public EntityModel<LinhaDeCreditoDto> encontrarLinhaDeCredito(Long id){

        if(id == null){
            throw new NullPointerException("Id não pode ser nulo");
        }
        
        LinhaDeCredito linhaDeCredito = linhaDeCreditoRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Linha de crédito não encontrada com o id: " + id));

        LinhaDeCreditoDto linhaDeCreditoDto = conversorDeObjetos.LinhaDeCreditoEntityParaDto(linhaDeCredito);
        
        Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(id)).withSelfRel().withType("GET");
        
        return EntityModel.of(linhaDeCreditoDto, selfLink);
    }


    // Metodo para encontrar todas as linhas de credito do mesmo tipo
    public CollectionModel<EntityModel<LinhaDeCreditoDto>> encontrarLinhaDeCreditoPorTipo(String tipo){
        
        TipoLinhaDeCredito tipoLinhaDeCredito = obterTipoLinhaDeCredito(tipo);
        List<LinhaDeCredito> linhaDeCreditoLista = linhaDeCreditoRepository.findByTipo(tipoLinhaDeCredito);
        
        List<EntityModel<LinhaDeCreditoDto>> entityModelList = linhaDeCreditoLista.stream()
        .map(linhaDeCredito -> {
            LinhaDeCreditoDto dto = conversorDeObjetos.LinhaDeCreditoEntityParaDto(linhaDeCredito);
            Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCredito.getIdLinhaDeCredito())).withSelfRel().withType("GET");
            return EntityModel.of(dto, selfLink);
        }).collect(Collectors.toList());       
    
        Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCreditoPorTipo(tipo)).withSelfRel().withType("GET");
        Link allLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
        CollectionModel.of(entityModelList, selfLink, allLink);
    
        return CollectionModel.of(entityModelList, selfLink, allLink);
    }


    // Metodo utilitario para transformar e padronizar a String do PathVariable em TipoLinhaDeCredito
    private TipoLinhaDeCredito obterTipoLinhaDeCredito(String tipo) {
        
        try {
            String tipoFormatado = tipo.replaceAll("-", "_").toUpperCase();            
            return TipoLinhaDeCredito.valueOf(tipoFormatado);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("O tipo " + tipo + " não existe em nosso sistema");
        
        } catch (NullPointerException e){
            throw new NullPointerException("O tipo não pode ser nulo");
        }
    }
}
