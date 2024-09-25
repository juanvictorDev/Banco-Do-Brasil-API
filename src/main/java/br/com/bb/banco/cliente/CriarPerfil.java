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
            registroDeDividas = random.nextInt(301); 
            dividas = Math.round((rendaMensal * 1.1) * 100.0) / 100.0;
            consultasAoCpf = random.nextInt(301); 
        }
      

        if (idade < 25) {
            evolucaoFinanceira = random.nextInt(301) + 100;

        } else if (idade < 40) {
            evolucaoFinanceira = random.nextInt(301) + 400; 

        } else {
            evolucaoFinanceira = random.nextInt(301) + 700;
        }
        
        score = 
            compromissoComCredito * 0.55 + 
            registroDeDividas * 0.33 + 
            consultasAoCpf * 0.06 + 
            evolucaoFinanceira * 0.06;

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


    private Map<String, Double> filtrarDados(double rendaMensal, double dividas, int idade, Ocupacao ocupacao, double compromissoComCredito){
        double capacidadeDePagamento, utilizacaoDeCredito, garantias;
        double estabilidadeProfissional, notaDoPerfil;
        
        if(dividas <= rendaMensal * 0.3){
            capacidadeDePagamento = random.nextInt(3) + 8; 

        } else if ( dividas <= rendaMensal * 0.6){
            capacidadeDePagamento = random.nextInt(4) + 4; 

        } else if (dividas <= rendaMensal * 0.9) {
            capacidadeDePagamento = random.nextInt(2) + 2;

        } else {
            capacidadeDePagamento = 0;
        }

        utilizacaoDeCredito = compromissoComCredito / 100; 

        if (idade < 25) {
            garantias = random.nextInt(3);

        } else if (idade < 40) {
            garantias = random.nextInt(4) + 1;

        } else {
            garantias = random.nextInt(5) + 2;
        }

        List<Ocupacao> ocupacaoBaixoNivel = Arrays.asList(Ocupacao.DESEMPREGADO, Ocupacao.ESTUDANTE);
        List<Ocupacao> ocupacaoAltoNivel = Arrays.asList(
            Ocupacao.MEDICO, Ocupacao.JUIZ, 
            Ocupacao.ECONOMISTA, Ocupacao.DESENVOLVEDOR_DE_SOFTWARE,
            Ocupacao.FISIOTERAPEUTA, Ocupacao.DENTISTA
        );

        if(ocupacaoAltoNivel.contains(ocupacao)){
            estabilidadeProfissional = random.nextInt(3) + 8;
        
        } else if (ocupacaoBaixoNivel.contains(ocupacao)){
            estabilidadeProfissional = random.nextInt(4);
            
        } else {
            estabilidadeProfissional = random.nextInt(3) + 5;
        }

        notaDoPerfil = 
            capacidadeDePagamento * 0.5 +
            utilizacaoDeCredito * 0.2 +
            garantias * 0.1 +
            estabilidadeProfissional * 0.1;
            
        notaDoPerfil = Math.round(notaDoPerfil * 100.0) / 100.0;

        Map<String, Double> resultadoFiltragemDeDados = new HashMap<>();
        resultadoFiltragemDeDados.put("capacidade de pagamento", capacidadeDePagamento);
        resultadoFiltragemDeDados.put("utilizacao de credito", utilizacaoDeCredito);
        resultadoFiltragemDeDados.put("garantias", garantias);
        resultadoFiltragemDeDados.put("estabilidade profissional", estabilidadeProfissional);
        resultadoFiltragemDeDados.put("nota do perfil", notaDoPerfil);

        return resultadoFiltragemDeDados;
    }


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
            .idPerfil(null)
            .notaDoPerfil(resultadoFiltragem.get("nota do perfil"))
            .score(resultadoScore.get("score"))
            .avaliacao(resultadoAvaliacao)
            .idCliente(clienteDados)
            .build();

        return clientePerfilRepository.save(clientePerfil);
    }

 
}
