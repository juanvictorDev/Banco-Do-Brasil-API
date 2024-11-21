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

    // -- ANTECIPACAO --

    // 13º SALARIO
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

    

    // RESTITUIÃO DO IRPF
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



    // SAQUE ANIVERSARIO FGTS
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


    // -- EMPRESTIMO COM GARANTIA --
    
    // GARANTIA DE VEICULO
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
    
    
    // GARANTIA DE IMOVEL
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
    
    
    // GARANTIA DE INVESTIMENTOS
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



    // -- FINANCIAMENTO --

    // FINANCIAMENTO IMOVEL
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

    // FINANCIAMENTO CARRO
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

    // FINANCIAMENTO MOTO
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

    // CREDITO MOBILIDADE
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
    
    // CREDITO REALIZA
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

    // CREDITO ENERGIA RENOVAVEL
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

    // CREDITO PCD
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
    
    
    // -- CDC
    
    // CREDITO AUTOMATICO
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

    // CREDITO SALARIO
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

    // CREDITO BENEFICIO
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


    //Metodo utilitario para calcular juros compostos
    private Map<String, BigDecimal> jurosCompostos(LinhaDeCreditoDto linhaDeCreditoDto, Integer periodo, Float capital){

        String jurosFixoOuVariavel = calcularJurosVariavel(periodo, linhaDeCreditoDto);

        BigDecimal taxaDeJuros = new BigDecimal(jurosFixoOuVariavel).divide(new BigDecimal("100"));
        
        BigDecimal fatorDeJuros = taxaDeJuros.add(BigDecimal.ONE).pow(periodo);

        BigDecimal montante = new BigDecimal(capital.toString()).multiply(fatorDeJuros);

        BigDecimal taxaDeJurosTotal = taxaDeJuros.add(BigDecimal.ONE).pow(periodo).subtract(BigDecimal.ONE).multiply(new BigDecimal("100"));

        BigDecimal valorDasParcelas = montante.divide(new BigDecimal(periodo.toString()), RoundingMode.HALF_UP);
        
        return Map.of("montante", montante, "taxaDeJurosTotal", taxaDeJurosTotal, "valorDasParcelas", valorDasParcelas, "taxaDeJurosVariavel", new BigDecimal(jurosFixoOuVariavel));
    }


    //Metodo utilitario para calcular limite de credito
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

    //Metodo utilitario para calcular juros variavel em tipos de linhas especificos com base nas parcelas
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
    }
}
