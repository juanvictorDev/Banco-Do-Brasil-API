package br.com.bb.banco.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import br.com.bb.banco.cliente.CalculadoraDeJuros;
import br.com.bb.banco.controller.LinhasDeCreditoController;
import br.com.bb.banco.dto.LinhaDeCreditoDto;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.entity.types.Avaliacao;
import br.com.bb.banco.entity.types.TipoLinhaDeCredito;
import br.com.bb.banco.error.custom.CreditLineValidationException;
import br.com.bb.banco.repository.ClienteDadosRepository;
import br.com.bb.banco.repository.ClientePerfilRepository;
import br.com.bb.banco.repository.LinhaDeCreditoRepository;
import br.com.bb.banco.security.UserDetailsImpl;
import br.com.bb.banco.utils.ConversorDeObjetos;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@Service
public class LinhasDeCreditoService {
    
    LinhaDeCreditoRepository linhaDeCreditoRepository;

    ClienteDadosRepository clienteDadosRepository;

    ClientePerfilRepository clientePerfilRepository;

    ConversorDeObjetos conversorDeObjetos;

    CalculadoraDeJuros calculadoraDeJuros;

    
    public LinhasDeCreditoService(
        LinhaDeCreditoRepository linhaDeCreditoRepository,
        ClienteDadosRepository clienteDadosRepository, 
        ClientePerfilRepository clientePerfilRepository,
        ConversorDeObjetos conversorDeObjetos, 
        CalculadoraDeJuros calculadoraDeJuros
    ) {
        this.linhaDeCreditoRepository = linhaDeCreditoRepository;
        this.clienteDadosRepository = clienteDadosRepository;
        this.clientePerfilRepository = clientePerfilRepository;
        this.conversorDeObjetos = conversorDeObjetos;
        this.calculadoraDeJuros = calculadoraDeJuros;
    }

    // [TODAS AS LINHAS DE CRIDITO]
    // Metodo para retornar todas as linhas de credito existentes de forma paginada
    public PagedModel<EntityModel<LinhaDeCreditoDto>> encontrarLinhasDeCredito(Integer page, Integer size){

        Pageable pageable = PageRequest.of(page, size);
        Page<LinhaDeCredito> paginaDeLinhaDeCredito = linhaDeCreditoRepository.findAll(pageable);

        List<EntityModel<LinhaDeCreditoDto>> entityModelList = paginaDeLinhaDeCredito.getContent().stream()
        .map(linhaDeCredito -> {
            LinhaDeCreditoDto dto = conversorDeObjetos.linhaDeCreditoEntityParaDto(linhaDeCredito);

            String tipoFormatado = linhaDeCredito.getTipo().toString().replaceAll("_", "-").toLowerCase();
            
            Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCredito.getIdLinhaDeCredito())).withSelfRel().withType("GET");
            Link simular = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipoFormatado, null, null, null, null)).withRel("simular").withType("GET");
            return EntityModel.of(dto, selfLink, simular);
        })
        .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            pageable.getPageSize(),
            paginaDeLinhaDeCredito.getNumber(),
            paginaDeLinhaDeCredito.getTotalElements()
        );

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page, size)).withSelfRel().withType("GET"));
        
        if (!paginaDeLinhaDeCredito.isLast() && paginaDeLinhaDeCredito.hasNext() && paginaDeLinhaDeCredito.hasContent()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page + 1, size)).withRel("next").withType("GET"));
        }
        
        if (!paginaDeLinhaDeCredito.isFirst() && paginaDeLinhaDeCredito.hasPrevious() && paginaDeLinhaDeCredito.hasContent()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(page - 1, size)).withRel("previous").withType("GET"));
        }
        
        if (!paginaDeLinhaDeCredito.isFirst() && paginaDeLinhaDeCredito.hasPrevious() && paginaDeLinhaDeCredito.hasContent()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, size)).withRel("firstPage").withType("GET"));
        }
        
        if (!paginaDeLinhaDeCredito.isLast() && paginaDeLinhaDeCredito.hasNext() && paginaDeLinhaDeCredito.hasContent()) {
            links.add(linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(paginaDeLinhaDeCredito.getTotalPages() - 1, size)).withRel("lastPage").withType("GET"));
        }

        return PagedModel.of(entityModelList, pageMetadata, links);
    }

    // [LINHA DE CREDITO ESPECIFICA]
    // Metodo para encontrar linha de credito pelo id especifico
    public EntityModel<LinhaDeCreditoDto> encontrarLinhaDeCredito(Long id){

        if(id == null){
            throw new NullPointerException("Id não pode ser nulo");
        }
        
        LinhaDeCredito linhaDeCredito = linhaDeCreditoRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Linha de crédito não encontrada com o id: " + id));

        LinhaDeCreditoDto linhaDeCreditoDto = conversorDeObjetos.linhaDeCreditoEntityParaDto(linhaDeCredito);
        
        String tipoFormatado = linhaDeCredito.getTipo().toString().replaceAll("_", "-").toLowerCase();

        Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(id)).withSelfRel().withType("GET");
        Link simular = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipoFormatado, null, null, null, null)).withRel("simular").withType("GET");
        Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
        return EntityModel.of(linhaDeCreditoDto, selfLink, simular, geral);
    }

    // [SIMULAR]
    // Metodo para simular e retornar linha de credito pelo tipo, com os resultados derivados do calculo especifico da taxa de juros
    public EntityModel<LinhaDeCreditoDto> simularLinhaDeCreditoPorTipo(String tipo, Float valor, Integer parcelas, Float custo, LocalDate data) {
        
        TipoLinhaDeCredito tipoLinhaDeCredito = obterTipoLinhaDeCredito(tipo);
        
        switch (tipoLinhaDeCredito) {
            case TipoLinhaDeCredito.ANTECIPAR_DECIMO_TERCEIRO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.anteciparDecimoTerceiroSalario(valor, LocalDate.now(), linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, null, null, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }
            
            case TipoLinhaDeCredito.ANTECIPAR_IRPF:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.anteciparImpostoDeRenda(valor, LocalDate.now(), data, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, null, null, data)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }
            
            case TipoLinhaDeCredito.ANTECIPAR_FGTS:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
        
                    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    LocalDate aniversario = userDetails.getClienteConta().getClienteDados().getDataDeNascimento();

                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.anteciparSaqueAniversarioFgts(valor, LocalDate.now(), aniversario, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, null, null, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.GARANTIA_VEICULO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.garantiaDeVeiculo(valor, parcelas, custo, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }
            
            case TipoLinhaDeCredito.GARANTIA_IMOVEL:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.garantiaDeImovel(valor, parcelas, custo, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }
            
            case TipoLinhaDeCredito.GARANTIA_INVESTIMENTO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.garantiaDeInvestimentos(valor, parcelas, custo, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.FINANCIAMENTO_IMOBILIARIO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.financiamentoDeImovel(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.FINANCIAMENTO_CARRO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.financiamentoDeCarro(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.FINANCIAMENTO_MOTO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.financiamentoDeMoto(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.CREDITO_MOBILIDADE:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoMobilidade(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.CREDITO_REALIZA:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoRealiza(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.CREDITO_ENERGIA_RENOVAVEL:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoEnergiaRenovavel(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }
            
            case TipoLinhaDeCredito.BENS_SERVICOS_PCD:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoPcd(custo, parcelas, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, null, parcelas, custo, null)).withSelfRel().withType("GET");           
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }
            
            case TipoLinhaDeCredito.CREDITO_AUTOMATICO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    Map<String, ?> resultado = obterDadosCdc();

                    Float rendaMensal = (Float) resultado.get("rendaMensal");
                    Avaliacao avaliacao = (Avaliacao) resultado.get("avaliacao");

                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoAutomatico(valor, parcelas, rendaMensal, avaliacao, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, parcelas, null, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.CREDITO_SALARIO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    Map<String, ?> resultado = obterDadosCdc();

                    Float rendaMensal = (Float) resultado.get("rendaMensal");
                    Avaliacao avaliacao = (Avaliacao) resultado.get("avaliacao");

                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoSalario(valor, parcelas, rendaMensal, avaliacao, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, parcelas, null, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            case TipoLinhaDeCredito.CREDITO_BENEFICIO:
                {
                    LinhaDeCreditoDto linhaDeCreditoDto = carregarEConverterLinha(tipoLinhaDeCredito);
                    Map<String, ?> resultado = obterDadosCdc();

                    Float rendaMensal = (Float) resultado.get("rendaMensal");
                    Avaliacao avaliacao = (Avaliacao) resultado.get("avaliacao");

                    LinhaDeCreditoDto linhaDeCreditoDtoFormatada = calculadoraDeJuros.creditoBeneficio(valor, parcelas, rendaMensal, avaliacao, linhaDeCreditoDto);
                    
                    Link selfLink = linkTo(methodOn(LinhasDeCreditoController.class).simularLinhasDeCreditoPorTipo(tipo, valor, parcelas, null, null)).withSelfRel().withType("GET");
                    Link linha = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhaDeCredito(linhaDeCreditoDto.getIdLinhaDeCredito())).withRel("linha especifica").withType("GET");
                    Link geral = linkTo(methodOn(LinhasDeCreditoController.class).buscarLinhasDeCredito(0, 10)).withRel("todas as linhas").withType("GET");
                    return EntityModel.of(linhaDeCreditoDtoFormatada, selfLink, linha, geral);
                }

            default:
                throw new RuntimeException("Erro no sistema de verificação de linhas");
        }
    }


    // Metodo utilitario para transformar e padronizar a String do PathVariable em TipoLinhaDeCredito
    private TipoLinhaDeCredito obterTipoLinhaDeCredito(String tipo) {
        try {
            String tipoFormatado = tipo.replaceAll("-", "_").toUpperCase();            
            return TipoLinhaDeCredito.valueOf(tipoFormatado);

        } catch (RuntimeException e) {
            throw new CreditLineValidationException("O tipo " + tipo + " não existe em nosso sistema");
        } 
    }

    // Metodo utilitario para buscar linhas de credito do database e converter para o dto
    private LinhaDeCreditoDto carregarEConverterLinha(TipoLinhaDeCredito tipo){
        LinhaDeCredito linhaDeCreditoEntity = linhaDeCreditoRepository.findByTipo(tipo);
        return conversorDeObjetos.linhaDeCreditoEntityParaDto(linhaDeCreditoEntity);
    }

    
    // Metodo utilitario para obter a renda mensal do cliente e sua avaliação
    private Map<String, ?> obterDadosCdc(){

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long id = userDetails.getClienteConta().getClienteDados().getIdCliente();
        ClienteDados clienteDados = userDetails.getClienteConta().getClienteDados();

        ClientePerfil clientePerfil = clientePerfilRepository.findByIdCliente(id).get();

        return Map.of("rendaMensal", clienteDados.getRendaMensal(), "avaliacao", clientePerfil.getAvaliacao());
    }
}
