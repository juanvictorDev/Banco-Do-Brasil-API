package br.com.bb.banco.entity;

import java.time.LocalDate;
import br.com.bb.banco.entity.types.Escolaridade;
import br.com.bb.banco.entity.types.EstadoCivil;
import br.com.bb.banco.entity.types.Ocupacao;
import br.com.bb.banco.entity.types.PessoaComDeficiencia;
import br.com.bb.banco.entity.types.Sexo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @NoArgsConstructor 
@AllArgsConstructor @Builder
@Entity @Table(name = "cliente_dados")
public class ClienteDados {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idCliente;

    @Column(nullable = false)
    String nome;
    
    @Column(nullable = false, unique = true)
    String cpf;
    
    @Column(nullable = false, unique = true)
    String email;
    
    @Column(nullable = false)
    String senha;

    @Column(nullable = false, unique = true)
    String telefone;

    @Column(nullable = false)
    LocalDate dataDeNascimento;

    @Column(nullable = false)
    String cep;

    @Column(nullable = false)
    String estado;

    @Column(nullable = false)
    String cidade;

    @Column(nullable = false)
    String bairro;

    @Column(nullable = false)
    String rua;

    @Column(nullable = false)
    String numeroResidencia;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    PessoaComDeficiencia pcd;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Sexo sexo;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Escolaridade escolaridade;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    EstadoCivil estadoCivil;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Ocupacao ocupacao;
    
    @Column(nullable = false)
    Double rendaMensal;

    @OneToOne(
        mappedBy = "clienteDados",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    ClienteConta clienteConta;
    
    @OneToOne(
        mappedBy = "clienteDados", 
        cascade = CascadeType.ALL, 
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    ClientePerfil clientePerfil;
}
