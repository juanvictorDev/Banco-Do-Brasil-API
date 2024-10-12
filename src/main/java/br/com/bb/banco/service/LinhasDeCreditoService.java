package br.com.bb.banco.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.repository.LinhaDeCreditoRepository;


@Service
public class LinhasDeCreditoService {
    
    @Autowired
    LinhaDeCreditoRepository linhaDeCreditoRepository;


    public CollectionModel<EntityModel<LinhaDeCredito>> encontrarLinhasDeCredito(){
        List<LinhaDeCredito> linhasDeCredito = linhaDeCreditoRepository.findAll();

        List<EntityModel<LinhaDeCredito>> entityModelList = linhasDeCredito.stream().map(linhaDeCredito -> {
            return EntityModel.of(linhaDeCredito);
        }).collect(Collectors.toList());

        return CollectionModel.of(entityModelList);    
    }

}
