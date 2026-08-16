package lu.police.pcms.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI pour la documentation de l'API PCMS.
 *
 * <p>
 * Cette configuration définit les métadonnées globales de l'API
 * utilisées par Swagger UI et le fichier openapi.json.
 * </p>
 *
 * <p>
 * Les informations sont visibles dans l'interface Swagger UI
 * sous la section "Info".
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PCMS - Police Case Management System API")
                        .version("1.0.0")
                        .description("""
                                API REST complète pour la gestion des dossiers d'enquête.
                                
                                **Fonctionnalités principales :**
                                - Gestion des rôles et autorisations
                                - Gestion des utilisateurs et départements
                                - Gestion des dossiers d'enquête (CRUD complet)
                                - Gestion des affectations (enquêteurs ↔ dossiers)
                                - Gestion des suspects, pièces jointes, commentaires
                                - Journalisation automatique des actions (AuditLog)
                                """)
                        .termsOfService("https://github.com/syllaaboubacar/PCMS")
                        .contact(new Contact()
                                .name("Aboubacar Sylla")
                                .email("aboubacar.sylla@pcms.lu")
                                .url("https://github.com/syllaaboubacar"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}