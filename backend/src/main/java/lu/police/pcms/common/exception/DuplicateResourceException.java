package lu.police.pcms.common.exception;

/**
 * Exception levée lorsqu'une tentative de création ou mise à jour
 * viole une contrainte d'unicité métier.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String entityName, String field, String value) {
        super(String.format("%s avec %s '%s' existe déjà.", entityName, field, value));
    }
}