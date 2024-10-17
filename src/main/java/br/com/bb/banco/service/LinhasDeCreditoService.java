package br.com.bb.banco.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import br.com.bb.banco.controller.LinhasDeCreditoController;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.repository.LinhaDeCreditoRepository;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@Service
public class LinhasDeCreditoService {
    
    @Autowired
    LinhaDeCreditoRepository linhaDeCreditoRepository;


    public PagedModel<EntityModel<LinhaDeCredito>> encontrarLinhasDeCredito(Integer page, Integer size){

        Pageable pageable = PageRequest.of(page, size);

        Page<LinhaDeCredito> paginaDeLinhaDeCredito = linhaDeCreditoRepository.findAll(pageable);

        List<EntityModel<LinhaDeCredito>> entityModelList = paginaDeLinhaDeCredito.getContent()
        .stream()
        .map(linhaDeCredito -> {
            Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCredito.getIdLinhaDeCredito())).withSelfRel().withType("GET");
            return EntityModel.of(linhaDeCredito, selfLink);
        })
        .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            pageable.getPageSize(),
            paginaDeLinhaDeCredito.getNumber(),
            paginaDeLinhaDeCredito.getTotalElements()
        );

        List<Link> links = new ArrayList<>();
        
        Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page, size)).withSelfRel().withType("GET");
        
        links.add(selfLink);

        if(!paginaDeLinhaDeCredito.isLast()){
            Link nextLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page + 1, size)).withRel("next").withType("GET");
            links.add(nextLink);
        }
        
        if(!paginaDeLinhaDeCredito.isFirst() && paginaDeLinhaDeCredito.hasContent()){
            Link prevLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page - 1, size)).withRel("previous").withType("GET");
            links.add(prevLink);
        }

        if(paginaDeLinhaDeCredito.hasPrevious()){
            Link firstLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, size)).withRel("firstPage").withType("GET");
            links.add(firstLink);
        }
        
        if(paginaDeLinhaDeCredito.hasNext() || !paginaDeLinhaDeCredito.hasContent()){
            Link lastLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(paginaDeLinhaDeCredito.getTotalPages() - 1, size)).withRel("lastPage").withType("GET");
            links.add(lastLink);
        }

        PagedModel<EntityModel<LinhaDeCredito>> pagedModel = PagedModel.of(entityModelList, pageMetadata, links);

        return pagedModel;
    }


    public EntityModel<LinhaDeCredito> encontrarLinhaDeCredito(Long id){
        
        LinhaDeCredito linhaDeCredito = linhaDeCreditoRepository.findById(id).get();
        
        Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(id)).withSelfRel().withType("GET");
        
        EntityModel<LinhaDeCredito> entityModel = EntityModel.of(linhaDeCredito, selfLink);

        return entityModel;
    }
}
