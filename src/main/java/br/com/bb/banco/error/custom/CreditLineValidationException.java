package br.com.bb.banco.error.custom;

public class CreditLineValidationException extends RuntimeException{
  
    public CreditLineValidationException(String message){ 
        super(message); 
    }

}
