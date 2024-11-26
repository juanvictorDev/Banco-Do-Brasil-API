package br.com.bb.banco.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;


@Service
public class JwtUtils {
    
    private static final String SECRET_KEY = "javaMelhorQuePython";
    private static final String ISSUER = "bb-api";
    

    /**
     * Gera um token JWT com base nos detalhes do usuário
     * @param userDetails detalhes do usuário autenticado
     * @return token JWT gerado
     * @throws RuntimeException se houver erro na geração do token
     */
    public String gerarTokenJwt(UserDetailsImpl userDetails) {
        try {
            Instant criacaoToken = LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"));
            Instant expiracaoToken = LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
        
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            String jwt = JWT.create()
            .withIssuer(ISSUER)
            .withIssuedAt(criacaoToken)
            .withExpiresAt(expiracaoToken)
            .withSubject(userDetails.getUsername())
            .withClaim("agencia", userDetails.getClienteConta().getAgencia()) 
            .withClaim("conta", userDetails.getClienteConta().getNumeroDaConta())     
            .sign(algorithm);
            
            return jwt;

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token");
        }
    }

    /**
     * Valida um token JWT e retorna suas claims
     * @param token token JWT a ser validado
     * @return Map contendo as claims do token
     * @throws RuntimeException se o token for inválido ou estiver expirado
     */
    public Map<String, Claim> validarTokenJwt(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            Map<String, Claim> claimsJwtDecoded = JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build()
            .verify(token)
            .getClaims();

            return claimsJwtDecoded;

        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token invalido ou expirado");
        }
    }
}
