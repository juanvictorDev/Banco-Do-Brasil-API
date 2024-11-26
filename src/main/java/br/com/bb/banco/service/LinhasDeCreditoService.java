package br.com.bb.banco.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * [TODAS AS LINHAS DE CREDITO]
     * Retorna todas as linhas de crédito existentes de forma paginada
     * @param page número da página desejada (começa em 0)
     * @param size quantidade de itens por página
     * @return PagedModel contendo EntityModels de LinhaDeCreditoDto com links HATEOAS
     * O retorno inclui:
     * - Lista de linhas de crédito com links individuais para:
     *   - self: link para detalhes da própria linha
     *   - simular: link para simulação da linha específica
     * - Metadados da paginação
     * - Links de navegação:
     *   - self: página atual
     *   - next: próxima página (se existir)
     *   - previous: página anterior (se existir) 
     *   - firstPage: primeira página (se não estiver nela)
     *   - lastPage: última página (se não estiver nela)
     */
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

    /**
     * [LINHA DE CREDITO ESPECIFICA]
     * Método para encontrar linha de crédito pelo id específico.
     * @param id Identificador único da linha de crédito
     * @return EntityModel contendo a linha de crédito encontrada e seus links HATEOAS
     * @throws NullPointerException se o id fornecido for nulo
     * @throws NoSuchElementException se não encontrar linha de crédito com o id fornecido
     */
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


    /**
     * [SIMULAR]
     * Simula uma linha de crédito com base no tipo, valor e outros parâmetros fornecidos.
     * O método avalia o perfil do cliente, verifica a elegibilidade para o tipo de crédito
     * solicitado e realiza o cálculo da taxa de juros específica para o tipo de linha de crédito.
     * Retorna os detalhes formatados da linha de crédito simulada com links relevantes.
     * @param tipo o tipo de linha de crédito solicitado (exemplo, "ANTECIPAR_DECIMO_TERCEIRO").
     * @param valor o valor desejado para a linha de crédito.
     * @param parcelas o número de parcelas para o pagamento do crédito (pode ser {@code null} para certos tipos).
     * @param custo o custo do bem ou patrimonio (pode ser {@code null} para certos tipos).
     * @param data uma data relevante para a simulação (e.g., data de vencimento ou retirada, pode ser {@code null}).
     * @return um {@link EntityModel} contendo os detalhes da linha de crédito simulada e links associados.
     * @throws RuntimeException se o cliente não atender aos critérios de elegibilidade para a linha de crédito solicitada
     * ou se ocorrer um erro no sistema de verificação.
     */
    public EntityModel<LinhaDeCreditoDto> simularLinhaDeCreditoPorTipo(String tipo, Float valor, Integer parcelas, Float custo, LocalDate data) {
        
        TipoLinhaDeCredito tipoLinhaDeCredito = obterTipoLinhaDeCredito(tipo);

        ClientePerfil clientePerfil = obterClientePerfil();

        if (clientePerfil.getAvaliacao().equals(Avaliacao.RUIM)) {

            List<TipoLinhaDeCredito> tiposPermitidos = Arrays.asList(
                TipoLinhaDeCredito.ANTECIPAR_DECIMO_TERCEIRO, 
                TipoLinhaDeCredito.ANTECIPAR_IRPF, 
                TipoLinhaDeCredito.ANTECIPAR_FGTS, 
                TipoLinhaDeCredito.GARANTIA_IMOVEL, 
                TipoLinhaDeCredito.GARANTIA_VEICULO, 
                TipoLinhaDeCredito.GARANTIA_INVESTIMENTO
            );
            
            if (!tiposPermitidos.contains(tipoLinhaDeCredito)) {
                throw new RuntimeException("Cliente não possui nota suficiente para solicitar linhas de crédito.");
            }
        }
           

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


    /**
     * Metodo utilitario para transformar e padronizar a String do PathVariable em TipoLinhaDeCredito
     * @param tipo String contendo o tipo de linha de crédito a ser convertido
     * @return TipoLinhaDeCredito enum correspondente ao tipo informado
     * @throws CreditLineValidationException quando o tipo informado não existe no sistema
     */
    private TipoLinhaDeCredito obterTipoLinhaDeCredito(String tipo) {
        try {
            String tipoFormatado = tipo.replaceAll("-", "_").toUpperCase();            
            return TipoLinhaDeCredito.valueOf(tipoFormatado);

        } catch (RuntimeException e) {
            throw new CreditLineValidationException("O tipo " + tipo + " não existe em nosso sistema");
        } 
    }

    /**
     * Metodo utilitario para buscar linhas de credito do database e converter para o dto
     * @param tipo TipoLinhaDeCredito enum que representa o tipo de linha de crédito a ser buscada
     * @return LinhaDeCreditoDto objeto contendo os dados da linha de crédito convertidos
     */
    private LinhaDeCreditoDto carregarEConverterLinha(TipoLinhaDeCredito tipo){
        LinhaDeCredito linhaDeCreditoEntity = linhaDeCreditoRepository.findByTipo(tipo);
        return conversorDeObjetos.linhaDeCreditoEntityParaDto(linhaDeCreditoEntity);
    }
    
    /**
     * Metodo utilitario para obter a renda mensal do cliente e sua avaliação
     * @return Map contendo a renda mensal e avaliação do cliente autenticado
     */
    private Map<String, ?> obterDadosCdc(){

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long id = userDetails.getClienteConta().getClienteDados().getIdCliente();
        ClienteDados clienteDados = userDetails.getClienteConta().getClienteDados();

        ClientePerfil clientePerfil = clientePerfilRepository.findByIdCliente(id).get();

        return Map.of("rendaMensal", clienteDados.getRendaMensal(), "avaliacao", clientePerfil.getAvaliacao());
    }

    /**
     * Metodo utilitario para obter o cliente perfil do usuario logado
     * @return ClientePerfil objeto contendo o perfil do cliente autenticado
     */
    private ClientePerfil obterClientePerfil() {
        return 
        ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .getClienteConta().getClienteDados().getClientePerfil();
    }
}
