package br.com.bb.banco.cliente;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.entity.types.Avaliacao;
import br.com.bb.banco.entity.types.Ocupacao;
import br.com.bb.banco.repository.ClientePerfilRepository;


@Service
public class CriarPerfil {
    
    private ClientePerfilRepository clientePerfilRepository;

    private Random random;

    public CriarPerfil(ClientePerfilRepository clientePerfilRepository) {
        this.clientePerfilRepository = clientePerfilRepository;
        this.random = new Random();
    }

    /* 
    ┌───────────────────────────────────────────────────────────────────────┐
    │ Os metodos abaixo foram criados para simular uma pesquisa de analise  │
    │ de credito e score feita pelas instituições financeiras como bancos,  │
    │ serasa e spc, a criação de perfil é feita no ato do registro do novo  │
    │ cliente, e no final recebendo uma avaliacao e uma nota para o perfil  │
    └───────────────────────────────────────────────────────────────────────┘
    */

    /**
     * @param idade Idade do cliente
     * @param rendaMensal Renda mensal do cliente em reais
     * @return Map contendo os seguintes dados:
     *         - compromisso com credito: está relacionado com o pagamento em dia de faturas como financiamentos, parcelamentos em lojas e empréstimos.
     *         - registro de dividas: representa o histórico de dívidas.
     *         - consultas ao cpf: são as consultas que as empresas fazem antes de conceder crédito, para conhecer o perfil financeiro do cliente.
     *         - evolucao financeira: está relacionado ao tempo de relacionamento do usuário com o mercado de crédito e seu histórico.
     *         - dividas: valor calculado com base na renda mensal.
     *         - score: pontuação final calculada com base em todos os fatores acima.
     */
    private Map<String, Double> pesquisarScore(int idade, double rendaMensal){
        double compromissoComCredito, registroDeDividas, consultasAoCpf;
        double dividas, evolucaoFinanceira, score;

        compromissoComCredito = random.nextInt(901) + 100;

        if(compromissoComCredito >= 701){
            registroDeDividas = random.nextInt(300) + 701; 
            dividas = Math.round((rendaMensal * 0.20) * 100.0) / 100.0;
            consultasAoCpf = random.nextInt(300) + 701; 
      
        }else if(compromissoComCredito >= 501 && compromissoComCredito <= 700){
            registroDeDividas = random.nextInt(200) + 501; 
            dividas = Math.round((rendaMensal * 0.30) * 100.0) / 100.0;
            consultasAoCpf = random.nextInt(200) + 501; 
            
        }else if(compromissoComCredito >= 301 && compromissoComCredito <= 500){
            registroDeDividas = random.nextInt(200) + 301; 
            dividas = Math.round((rendaMensal * 0.75) * 100.0) / 100.0;
            consultasAoCpf = random.nextInt(200) + 301; 
            
        }else{
            registroDeDividas = random.nextInt(200) + 101; 
            dividas = Math.round((rendaMensal * 1.1) * 100.0) / 100.0;
            consultasAoCpf = random.nextInt(301); 
        }
      

        if (idade <= 25) {
            evolucaoFinanceira = random.nextInt(301) + 100;

        } else if (idade <= 40) {
            evolucaoFinanceira = random.nextInt(301) + 400; 

        } else {
            evolucaoFinanceira = random.nextInt(301) + 700;
        }
        
        score = 
            ( compromissoComCredito * 0.55 + registroDeDividas * 0.33 + 
            consultasAoCpf * 0.06 + evolucaoFinanceira * 0.06 ) / ( 0.55 + 0.33 + 0.06 + 0.06 );

        score = Math.round(score * 100.0) / 100.0;
        
        Map<String, Double> resultadoPesquisaDeScore = new HashMap<>();
        resultadoPesquisaDeScore.put("compromisso com credito", compromissoComCredito);
        resultadoPesquisaDeScore.put("registro de dividas", registroDeDividas);
        resultadoPesquisaDeScore.put("consultas ao cpf", consultasAoCpf);
        resultadoPesquisaDeScore.put("evolucao financeira", evolucaoFinanceira);
        resultadoPesquisaDeScore.put("dividas", dividas);
        resultadoPesquisaDeScore.put("score", score);

        return resultadoPesquisaDeScore;
    }


    /**
     * Filtra e processa os dados do cliente para gerar uma nota de perfil
     * @param rendaMensal valor da renda mensal do cliente
     * @param dividas valor total das dívidas do cliente
     * @param idade idade do cliente
     * @param ocupacao ocupação profissional do cliente
     * @param compromissoComCredito pontuação que representa o histórico de compromissos com crédito
     * @return Map contendo os valores calculados:
     *         - capacidade de pagamento: relação entre a renda e as dívidas da pessoa.
     *         - utilização de crédito: mede quanto de crédito o cliente está utilizando.
     *         - garantias: quantidade de garantias que o cliente possui.
     *         - estabilidade profissional: é avaliada pelo histórico de empregos e a natureza do setor em que o cliente trabalha.
     *         - nota final do perfil: pontuação final calculada com base em todos os fatores acima.
     */
    private Map<String, Double> filtrarDados(double rendaMensal, double dividas, int idade, Ocupacao ocupacao, double compromissoComCredito){
        double capacidadeDePagamento, utilizacaoDeCredito, garantias;
        double estabilidadeProfissional, notaDoPerfil;
        
        if(dividas <= rendaMensal * 0.3){
            capacidadeDePagamento = random.nextInt(3) + 8; 

        } else if ( dividas <= rendaMensal * 0.6){
            capacidadeDePagamento = random.nextInt(4) + 4; 

        } else if (dividas <= rendaMensal * 0.9) {
            capacidadeDePagamento = random.nextInt(3) + 1;

        } else {
            capacidadeDePagamento = 0;
        }

        utilizacaoDeCredito = compromissoComCredito / 100; 

        if (idade < 25) {
            garantias = random.nextInt(4);

        } else if (idade < 40) {
            garantias = random.nextInt(4) + 3;

        } else {
            garantias = random.nextInt(5) + 6;
        }

        List<Ocupacao> ocupacaoBaixoNivel = Arrays.asList(Ocupacao.DESEMPREGADO, Ocupacao.ESTUDANTE);
        List<Ocupacao> ocupacaoAltoNivel = Arrays.asList(
            Ocupacao.MEDICO, Ocupacao.JUIZ, 
            Ocupacao.ECONOMISTA, Ocupacao.DESENVOLVEDOR_DE_SOFTWARE,
            Ocupacao.FISIOTERAPEUTA, Ocupacao.DENTISTA
        );

        if(ocupacaoAltoNivel.contains(ocupacao)){
            estabilidadeProfissional = 10;
        
        } else if (ocupacaoBaixoNivel.contains(ocupacao)){
            estabilidadeProfissional = 3;
            
        } else {
            estabilidadeProfissional = 7;
        }

        notaDoPerfil = 
            ( capacidadeDePagamento * 0.6 + utilizacaoDeCredito * 0.2 + 
            garantias * 0.1 + estabilidadeProfissional * 0.1 ) / ( 0.6 + 0.2 + 0.1 + 0.1 );
            
        notaDoPerfil = Math.round(notaDoPerfil * 100.0) / 100.0;

        Map<String, Double> resultadoFiltragemDeDados = new HashMap<>();
        resultadoFiltragemDeDados.put("capacidade de pagamento", capacidadeDePagamento);
        resultadoFiltragemDeDados.put("utilizacao de credito", utilizacaoDeCredito);
        resultadoFiltragemDeDados.put("garantias", garantias);
        resultadoFiltragemDeDados.put("estabilidade profissional", estabilidadeProfissional);
        resultadoFiltragemDeDados.put("nota do perfil", notaDoPerfil);

        return resultadoFiltragemDeDados;
    }

    /**
     * @param notaDoPerfil valor numérico entre 0 e 10 que representa a nota do perfil do cliente
     * @return Avaliacao enum que representa a classificação do cliente
     */
    private Avaliacao receberAvaliacao(double notaDoPerfil){
        Avaliacao avaliacao;

        if(notaDoPerfil >= 8){
            avaliacao = Avaliacao.EXCELENTE;

        } else if (notaDoPerfil >= 6) {
            avaliacao = Avaliacao.BOM;
        
        } else if (notaDoPerfil >= 4){
            avaliacao = Avaliacao.MEDIANO;
        
        } else{
            avaliacao = Avaliacao.RUIM;
        }

        return avaliacao;
    }

    /**
     * Metodo principal que chama outros metodos e que recebe os dados do novo cliente e 
     * cria seu perfil salvando no banco de dados
     * @param clienteDados objeto contendo os dados basicos do cliente como idade, renda e ocupacao
     * @return ClientePerfil objeto contendo o perfil completo do cliente com sua avaliacao de credito
     */
    public ClientePerfil criar(ClienteDados clienteDados){
        int idade = Period.between(clienteDados.getDataDeNascimento(), LocalDate.now()).getYears();
        double rendaMensal = clienteDados.getRendaMensal();
        Map<String, Double> resultadoScore = pesquisarScore(idade, rendaMensal);
        
        Ocupacao ocupacao = clienteDados.getOcupacao();
        double compromissoComCredito = resultadoScore.get("compromisso com credito");
        double dividas = resultadoScore.get("dividas");
        Map<String, Double> resultadoFiltragem = filtrarDados(rendaMensal, dividas, idade, ocupacao, compromissoComCredito);
        
        Avaliacao resultadoAvaliacao = receberAvaliacao(resultadoFiltragem.get("nota do perfil"));

        ClientePerfil clientePerfil = ClientePerfil.builder()
            .notaDoPerfil(resultadoFiltragem.get("nota do perfil"))
            .score(resultadoScore.get("score"))
            .avaliacao(resultadoAvaliacao)
            .clienteDados(clienteDados)
            .build();

        return clientePerfilRepository.save(clientePerfil);
    } 
}
