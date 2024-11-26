package br.com.bb.banco.cliente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import org.springframework.stereotype.Service;
import br.com.bb.banco.dto.LinhaDeCreditoDto;
import br.com.bb.banco.entity.types.Avaliacao;
import br.com.bb.banco.entity.types.TipoLinhaDeCredito;
import br.com.bb.banco.error.custom.CreditLineValidationException;


@Service
public class CalculadoraDeJuros {

    /* 
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                              ANTECIPAÇÃO                                    │
    └─────────────────────────────────────────────────────────────────────────────┘
    */

    /**
     * [13º SALARIO]
     * Calcula a antecipação do décimo terceiro salário
     * @param valor Valor do décimo terceiro salário a ser antecipado (entre R$1412.00 e R$20000.00)
     * @param dataAtual Data atual para cálculo do empréstimo
     * @param linhaDeCreditoDto Objeto contendo as informações da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do empréstimo
     * @throws CreditLineValidationException se o valor estiver fora dos limites ou se estiver nos meses de pagamento (11 ou 12)
     */
    public LinhaDeCreditoDto anteciparDecimoTerceiroSalario(Float valor, LocalDate dataAtual, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(dataAtual.getMonthValue() == 11 || dataAtual.getMonthValue() == 12){
            throw new CreditLineValidationException("Você não precisa pegar o empréstimo, o 13º salário está disponível para você");
        }
        
        if(valor < 1412 || valor > 20000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$1412.00 e o maximo R$20000.00");
        }
        
        LocalDate dezembro = LocalDate.of(dataAtual.getYear(), 12, 31);

        Integer numeroDeMeses = Math.toIntExact(Period.between(dataAtual, dezembro).toTotalMonths());

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, numeroDeMeses, valor);

        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setTaxaDeJurosTotal(resultado.get("taxaDeJurosTotal").floatValue());
        linhaDeCreditoDto.setValor(valor);
        linhaDeCreditoDto.setTaxaDeJuros(null);

        return linhaDeCreditoDto;
    }
    

    /**
     * [RESTITUIÇÃO DO IRPF]
     * Calcula a antecipação da restituição do Imposto de Renda
     * @param valor Valor da restituição a ser antecipada (entre R$1911.00 e R$50000.00)
     * @param dataAtual Data atual para cálculo do empréstimo
     * @param dataRecebimento Data prevista para recebimento da restituição
     * @param linhaDeCreditoDto Objeto contendo as informações da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do empréstimo
     * @throws CreditLineValidationException se o valor estiver fora dos limites, se a data de recebimento for igual à data atual ou se a data de recebimento for anterior à data atual
     */
    public LinhaDeCreditoDto anteciparImpostoDeRenda(Float valor, LocalDate dataAtual, LocalDate dataRecebimento, LinhaDeCreditoDto linhaDeCreditoDto){
                
        if(dataAtual.equals(dataRecebimento)){
            throw new CreditLineValidationException("Você não precisa pegar o empréstimo, o IRPF está disponível para você");
        }
        
        if(dataRecebimento.isBefore(dataAtual)){
            throw new CreditLineValidationException("Data invalida, não é possivel calcular o IRPF para você");
        }
        
        if(valor < 1911 || valor > 50000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$1911.00 e o maximo R$50000.00");
        }
    
        Integer numeroDeMeses = Math.toIntExact(Period.between(dataAtual, dataRecebimento).toTotalMonths());
        
        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, numeroDeMeses, valor);

        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setTaxaDeJurosTotal(resultado.get("taxaDeJurosTotal").floatValue());
        linhaDeCreditoDto.setValor(valor);
        linhaDeCreditoDto.setTaxaDeJuros(null);

        return linhaDeCreditoDto;
    }


    /**
     * [SAQUE ANIVERSARIO FGTS]
     * Calcula a antecipação do saque aniversário do FGTS
     * @param saldo Saldo disponível no FGTS (entre R$500.00 e R$50000.00)
     * @param dataAtual Data atual para cálculo do empréstimo
     * @param dataDeAniversario Data de aniversário do cliente para saque do FGTS
     * @param linhaDeCreditoDto Objeto contendo as informações da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do empréstimo
     * @throws CreditLineValidationException se o valor estiver fora dos limites ou se o mês atual for igual ao mês de aniversário
     */
    public LinhaDeCreditoDto anteciparSaqueAniversarioFgts(Float saldo, LocalDate dataAtual, LocalDate dataDeAniversario, LinhaDeCreditoDto linhaDeCreditoDto){
                    
        if(saldo < 500 || saldo > 50000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$500.00 e o maximo R$50000.00");
        }
        
        if(dataDeAniversario.getMonthValue() == dataAtual.getMonthValue()){
            throw new CreditLineValidationException("Você não precisa pegar o empréstimo, o Saque Aniversario FGTS está disponível para você");
        }
        
        LocalDate aniversario = LocalDate.of(dataAtual.getYear(), dataDeAniversario.getMonthValue(), dataDeAniversario.getDayOfMonth());
        
        Integer numeroDeMeses; 
        
        if (aniversario.isBefore(dataAtual)) {
            LocalDate aniversarioFuturo = aniversario.plusYears(1);
            numeroDeMeses = Math.toIntExact(Period.between(dataAtual, aniversarioFuturo).toTotalMonths());
        
        }else{
            numeroDeMeses = Math.toIntExact(Period.between(dataAtual, aniversario).toTotalMonths());
        }
        
        BigDecimal valor;

        if(saldo == 500){
            BigDecimal porcentagem = new BigDecimal("50");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100")));
            
        }else if(saldo >= 500.01 && saldo <= 1000){
            BigDecimal porcentagem = new BigDecimal("40");
            BigDecimal parcelaFixa = new BigDecimal("50.00");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100"))).add(parcelaFixa);

        }else if(saldo >= 1000.01 && saldo <= 5000){
            BigDecimal porcentagem = new BigDecimal("30");
            BigDecimal parcelaFixa = new BigDecimal("150.00");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100"))).add(parcelaFixa);
            
        }else if(saldo >= 5000.01 && saldo <= 10000){
            BigDecimal porcentagem = new BigDecimal("20");
            BigDecimal parcelaFixa = new BigDecimal("650.00");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100"))).add(parcelaFixa);
            
        }else if(saldo >= 10000.01 && saldo <= 15000){
            BigDecimal porcentagem = new BigDecimal("15");
            BigDecimal parcelaFixa = new BigDecimal("1150.00");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100"))).add(parcelaFixa);
            
        }else if(saldo >= 15000.01 && saldo <= 20000){
            BigDecimal porcentagem = new BigDecimal("10");
            BigDecimal parcelaFixa = new BigDecimal("1900.00");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100"))).add(parcelaFixa);
            
        }else{
            BigDecimal porcentagem = new BigDecimal("5");
            BigDecimal parcelaFixa = new BigDecimal("2900.00");
            valor = new BigDecimal(saldo.toString()).multiply(porcentagem.divide(new BigDecimal("100"))).add(parcelaFixa);
            
        }

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, numeroDeMeses, valor.floatValue());
    
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setTaxaDeJurosTotal(resultado.get("taxaDeJurosTotal").floatValue());
        linhaDeCreditoDto.setValor(valor.floatValue());
        linhaDeCreditoDto.setTaxaDeJuros(null);
    
        return linhaDeCreditoDto;
    }
    
    /* 
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                        EMPRESTIMO COM GARANTIA                              │
    └─────────────────────────────────────────────────────────────────────────────┘
    */

    /**
     * [GARANTIA DE VEICULO]
     * Calcula empréstimo com garantia de veículo
     * @param valor Valor do empréstimo solicitado
     * @param parcelas Número de parcelas desejado
     * @param custo Valor do veículo usado como garantia
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do empréstimo
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto garantiaDeVeiculo(Float valor, Integer parcelas, Float custo, LinhaDeCreditoDto linhaDeCreditoDto){
        

        if(valor > (custo * 0.8)){
            throw new CreditLineValidationException("Valor do emprestimo ultrapassa o limite de 80% do custo do veiculo");
        }

        if(valor < 10000 || valor > 1000000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$10.000,00 e o maximo R$1.000.000,00");
        }
        
        if(parcelas < 10 || parcelas > 60){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 20 e o maximo 60");
        }
            
        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor);

        linhaDeCreditoDto.setValor(valor);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }    
    
    /**
     * [GARANTIA DE IMOVEL]
     * Calcula empréstimo com garantia de imóvel
     * @param valor Valor do empréstimo solicitado
     * @param parcelas Número de parcelas desejado
     * @param custo Valor do imóvel usado como garantia
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do empréstimo
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto garantiaDeImovel(Float valor, Integer parcelas, Float custo, LinhaDeCreditoDto linhaDeCreditoDto){
        

        if(valor > (custo * 0.55)){
            throw new CreditLineValidationException("Valor do emprestimo ultrapassa o limite de 55% do custo do imovel");
        }

        if(valor < 35000 || valor > 5000000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$35.000,00 e o maximo R$5.000.000,00");
        }
        
        if(parcelas < 18 || parcelas > 240){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 18 e o maximo 240");
        }
            
        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor);

        linhaDeCreditoDto.setValor(valor);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }    
    
    /**
     * [GARANTIA DE INVESTIMENTOS]
     * Calcula empréstimo com garantia de investimentos
     * @param valor Valor do empréstimo solicitado
     * @param parcelas Número de parcelas desejado
     * @param custo Valor total dos investimentos usados como garantia
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do empréstimo
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto garantiaDeInvestimentos(Float valor, Integer parcelas, Float custo, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 10000){
            throw new CreditLineValidationException("Valor dos seus investimenos não é o suficiente, limite minimo de R$10.000,00");
        }
        
        if(valor < 10000 || valor > 10000000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$10.000,00 e o maximo R$10.000.000,00");
        }
        
        if(valor > 50000 && valor < 5000000 && valor > (custo * 0.55)){
            throw new CreditLineValidationException("Valor do emprestimo ultrapassa o limite de 55% do custo dos investimentos");
        }
        
        if(valor >= 5000000 && valor < 10000000 && valor > (custo * 0.10)){
            throw new CreditLineValidationException("Valor do emprestimo ultrapassa o limite de 10% do custo dos investimentos");
        }
        
        if(parcelas < 10 || parcelas > 72){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 10 e o maximo 72");
        }
            
        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor);

        linhaDeCreditoDto.setValor(valor);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /* 
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                             FINANCIAMENTO                                   │
    └─────────────────────────────────────────────────────────────────────────────┘
    */

    /**
     * [FINANCIAMENTO IMOVEL]
     * Calcula financiamento de imóvel com limite de 80% do valor do imóvel
     * @param custo Valor total do imóvel
     * @param parcelas Número de parcelas desejado
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto financiamentoDeImovel(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 20000 || custo > 5000000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$20.000,00 e o maximo R$5.000.000,00");
        }
        
        if(parcelas < 15 || parcelas > 420){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 15 e o maximo 420");
        }

        BigDecimal porcentagem = new BigDecimal("80");
        BigDecimal valor = new BigDecimal(custo.toString()).multiply(porcentagem.divide(new BigDecimal("100")));

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor.floatValue());

        linhaDeCreditoDto.setValor(valor.floatValue());
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }


    /**
     * [FINANCIAMENTO CARRO]
     * Calcula financiamento de carro
     * @param custo Valor total do carro
     * @param parcelas Número de parcelas desejado
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto financiamentoDeCarro(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 20000 || custo > 500000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$20.000,00 e o maximo R$500.000,00");
        }
        
        if(parcelas < 15 || parcelas > 72){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 15 e o maximo 72");
        }

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, custo);

        linhaDeCreditoDto.setValor(custo);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }


    /**
     * [FINANCIAMENTO MOTO]
     * Calcula financiamento de moto com limite de 70% do valor da moto
     * @param custo Valor total da moto
     * @param parcelas Número de parcelas desejado
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto financiamentoDeMoto(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 10000 || custo > 100000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$10.000,00 e o maximo R$100.000,00");
        }
        
        if(parcelas < 5 || parcelas > 36){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 5 e o maximo 36");
        }

        BigDecimal porcentagem = new BigDecimal("70");
        BigDecimal valor = new BigDecimal(custo.toString()).multiply(porcentagem.divide(new BigDecimal("100")));

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor.floatValue());

        linhaDeCreditoDto.setValor(valor.floatValue());
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /**
     * [CREDITO MOBILIDADE]
     * Calcula crédito mobilidade com valores entre R$500,00 e R$50.000,00
     * @param custo Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 5 e 60)
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoMobilidade(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 500 || custo > 50000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$500,00 e o maximo R$50.000,00");
        }
        
        if(parcelas < 5 || parcelas > 60){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 5 e o maximo 60");
        }

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, custo);

        linhaDeCreditoDto.setValor(custo);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }
    
    /**
     * [CREDITO REALIZA]
     * Calcula crédito realiza com valores entre R$100,00 e R$50.000,00
     * @param custo Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 1 e 60)
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoRealiza(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 100 || custo > 50000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$100,00 e o maximo R$50.000,00");
        }
        
        if(parcelas < 1 || parcelas > 60){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 1 e o maximo 60");
        }

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, custo);

        linhaDeCreditoDto.setValor(custo);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /**
     * [CREDITO ENERGIA RENOVAVEL]
     * Calcula crédito para projetos de energia renovável com valores entre R$2.000,00 e R$100.000,00
     * @param custo Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 2 e 96)
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoEnergiaRenovavel(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 2000 || custo > 100000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$2.000,00 e o maximo R$100.000,00");
        }
        
        if(parcelas < 2 || parcelas > 96){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 2 e o maximo 96");
        }

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, custo);

        linhaDeCreditoDto.setValor(custo);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /**
     * [CREDITO PCD]
     * Calcula crédito para Pessoas com Deficiência com valores entre R$70,00 e R$30.000,00
     * @param custo Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 2 e 60)
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoPcd(Float custo, Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(custo < 70 || custo > 30000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$70,00 e o maximo R$30.000,00");
        }
        
        if(parcelas < 2 || parcelas > 60){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 2 e o maximo 60");
        }

        Map<String, BigDecimal> resultado = jurosCompostos(linhaDeCreditoDto, parcelas, custo);

        linhaDeCreditoDto.setValor(custo);
        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }    
    
    /* 
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                                  CDC                                        │
    └─────────────────────────────────────────────────────────────────────────────┘
    */
    
    /**
     * [CREDITO AUTOMÁTICO]
     * Calcula crédito automático com valores entre R$500,00 e R$10.000.000,00
     * @param valor Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 2 e 72)
     * @param rendaMensal Renda mensal do cliente
     * @param avaliacao Avaliação de crédito do cliente
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoAutomatico(Float valor, Integer parcelas, Float rendaMensal, Avaliacao avaliacao, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(valor < 500 || valor > 10000000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$500,00 e o maximo R$10.000.000,00");
        }
        
        if(parcelas < 2 || parcelas > 72){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 2 e o maximo 72");
        }

        Float limiteDeCredito = calcularLimiteDeCredito(avaliacao, rendaMensal);

        Map<String, BigDecimal> resultado = null;

        if(valor > limiteDeCredito){
            resultado = jurosCompostos(linhaDeCreditoDto, parcelas, limiteDeCredito);
            linhaDeCreditoDto.setValor(limiteDeCredito);
    
        }else{
            resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor);
            linhaDeCreditoDto.setValor(valor);
        }

        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setTaxaDeJuros(resultado.get("taxaDeJurosVariavel").floatValue());
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /**
     * [CREDITO SALÁRIO]
     * Calcula crédito salário com valores entre R$500,00 e R$10.000.000,00
     * @param valor Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 2 e 96)
     * @param rendaMensal Renda mensal do cliente
     * @param avaliacao Avaliação de crédito do cliente
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoSalario(Float valor, Integer parcelas, Float rendaMensal, Avaliacao avaliacao, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(valor < 500 || valor > 10000000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$500,00 e o maximo R$10.000.000,00");
        }
        
        if(parcelas < 2 || parcelas > 96){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 2 e o maximo 96");
        }

        Float limiteDeCredito = calcularLimiteDeCredito(avaliacao, rendaMensal);

        Map<String, BigDecimal> resultado = null;

        if(valor > limiteDeCredito){
            resultado = jurosCompostos(linhaDeCreditoDto, parcelas, limiteDeCredito);
            linhaDeCreditoDto.setValor(limiteDeCredito);
    
        }else{
            resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor);
            linhaDeCreditoDto.setValor(valor);
        }

        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setTaxaDeJuros(resultado.get("taxaDeJurosVariavel").floatValue());
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /**
     * [CREDITO BENEFÍCIO]
     * Calcula crédito benefício com valores entre R$500,00 e R$39.000,00
     * @param valor Valor total solicitado
     * @param parcelas Número de parcelas desejado (entre 2 e 72)
     * @param rendaMensal Renda mensal do cliente
     * @param avaliacao Avaliação de crédito do cliente
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return LinhaDeCreditoDto com os valores calculados do financiamento
     * @throws CreditLineValidationException se os valores estiverem fora dos limites permitidos
     */
    public LinhaDeCreditoDto creditoBeneficio(Float valor, Integer parcelas, Float rendaMensal, Avaliacao avaliacao, LinhaDeCreditoDto linhaDeCreditoDto){
        
        if(valor < 500 || valor > 39000){
            throw new CreditLineValidationException("Você digitou um valor fora do nosso limite, o minimo é R$500,00 e o maximo R$39.000,00");
        }
        
        if(parcelas < 2 || parcelas > 72){
            throw new CreditLineValidationException("Você digitou um numero de parcelas fora do nosso limite, o minimo é 2 e o maximo 72");
        }

        Float limiteDeCredito = calcularLimiteDeCredito(avaliacao, rendaMensal);

        Map<String, BigDecimal> resultado = null;

        if(valor > limiteDeCredito){
            resultado = jurosCompostos(linhaDeCreditoDto, parcelas, limiteDeCredito);
            linhaDeCreditoDto.setValor(limiteDeCredito);
    
        }else{
            resultado = jurosCompostos(linhaDeCreditoDto, parcelas, valor);
            linhaDeCreditoDto.setValor(valor);
        }

        linhaDeCreditoDto.setMontante(resultado.get("montante").floatValue());
        linhaDeCreditoDto.setNumeroDeParcelas(parcelas);
        linhaDeCreditoDto.setTaxaDeJuros(resultado.get("taxaDeJurosVariavel").floatValue());
        linhaDeCreditoDto.setValorDaParcela(resultado.get("valorDasParcelas").floatValue());

        return linhaDeCreditoDto;
    }

    /* 
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                            METODOS UTILITARIOS                              │
    └─────────────────────────────────────────────────────────────────────────────┘
    */

    /**
     * Calcula juros compostos para uma linha de crédito
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @param periodo Número de parcelas ou meses
     * @param capital Valor do empréstimo
     * @return Map contendo montante, taxa de juros total, valor das parcelas e taxa de juros variável
     */
    private Map<String, BigDecimal> jurosCompostos(LinhaDeCreditoDto linhaDeCreditoDto, Integer periodo, Float capital){

        String jurosFixoOuVariavel = calcularJurosVariavel(periodo, linhaDeCreditoDto);

        BigDecimal taxaDeJuros = new BigDecimal(jurosFixoOuVariavel).divide(new BigDecimal("100"));
        
        BigDecimal fatorDeJuros = taxaDeJuros.add(BigDecimal.ONE).pow(periodo);

        BigDecimal montante = new BigDecimal(capital.toString()).multiply(fatorDeJuros);

        BigDecimal taxaDeJurosTotal = taxaDeJuros.add(BigDecimal.ONE).pow(periodo).subtract(BigDecimal.ONE).multiply(new BigDecimal("100"));

        BigDecimal valorDasParcelas = montante.divide(new BigDecimal(periodo.toString()), RoundingMode.HALF_UP);
        
        return Map.of("montante", montante, "taxaDeJurosTotal", taxaDeJurosTotal, "valorDasParcelas", valorDasParcelas, "taxaDeJurosVariavel", new BigDecimal(jurosFixoOuVariavel));
    }


    /**
     * Calcula o limite de crédito baseado na avaliação e renda mensal do cliente
     * @param avaliacao Avaliação de crédito do cliente (EXCELENTE, BOM ou outros)
     * @param rendaMensal Renda mensal do cliente
     * @return Limite de crédito calculado
     */
    private Float calcularLimiteDeCredito(Avaliacao avaliacao, Float rendaMensal){
        
        Float limiteDeCredito;
        
        if(Avaliacao.EXCELENTE.equals(avaliacao)){
            Integer porcentagem = 500 / 100;
            limiteDeCredito = rendaMensal * porcentagem;
            
        }else if(Avaliacao.BOM.equals(avaliacao)){
            Integer porcentagem = 300 / 100;
            limiteDeCredito = rendaMensal * porcentagem;

        }else{
            Integer porcentagem = 200 / 100;
            limiteDeCredito = rendaMensal * porcentagem;
        }
        
        return limiteDeCredito;
    }

    /**
     * Calcula a taxa de juros variável com base no tipo de linha de crédito e número de parcelas
     * @param parcelas Número de parcelas do empréstimo
     * @param linhaDeCreditoDto Objeto contendo dados da linha de crédito
     * @return Taxa de juros calculada em formato String
     */
    String calcularJurosVariavel(Integer parcelas, LinhaDeCreditoDto linhaDeCreditoDto){
        
        TipoLinhaDeCredito tipo = TipoLinhaDeCredito.valueOf(linhaDeCreditoDto.getTipo());
        Float taxaDeJuros = linhaDeCreditoDto.getTaxaDeJuros();

        if(TipoLinhaDeCredito.CREDITO_AUTOMATICO.equals(tipo)){
            if(parcelas >= 50 && parcelas <= 72){
                taxaDeJuros += 0.33f;
            }else if(parcelas >= 25 && parcelas <= 49){
                taxaDeJuros += 0.12f;
            }
        }

        if(TipoLinhaDeCredito.CREDITO_SALARIO.equals(tipo)){
            if(parcelas >= 60 && parcelas <= 96){
                taxaDeJuros += 0.25f;
            }else if(parcelas >= 30 && parcelas <= 59){
                taxaDeJuros += 0.1f;
            }
        }

        if(TipoLinhaDeCredito.CREDITO_BENEFICIO.equals(tipo)){
            if(parcelas >= 50 && parcelas <= 72){
                taxaDeJuros += 0.22f;
            }else if(parcelas >= 25 && parcelas <= 49){
                taxaDeJuros += 0.1f;
            }
        }

        return taxaDeJuros.toString();
    }}
