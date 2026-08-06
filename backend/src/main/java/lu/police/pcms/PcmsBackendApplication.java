package lu.police.pcms;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PcmsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PcmsBackendApplication.class, args);
	}

	/**
     * Bean fournissant le nom de l'utilisateur courant pour les champs d'audit
     * (createdBy, updatedBy). En l'absence de sécurité, on retourne "system" par défaut.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }

}
