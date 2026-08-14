package lu.police.pcms.common.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'existe pas en base de données.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityName, Long id) {
        super(String.format("%s introuvable avec l'identifiant : %d", entityName, id));
    }

    public ResourceNotFoundException(String entityName, String field, String value) {
        super(String.format("%s introuvable avec %s : %s", entityName, field, value));
    }
}