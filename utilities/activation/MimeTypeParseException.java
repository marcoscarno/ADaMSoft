package ADaMSoft.utilities.activation;

/**
 * A class to encapsulate MimeType parsing related exceptions.
 */
public class MimeTypeParseException extends Exception {

    /**
     * Constructs a MimeTypeParseException with no specified detail message.
     */
    public MimeTypeParseException() {
	super();
    }

    /**
     * Constructs a MimeTypeParseException with the specified detail message.
     *
     * @param   s   the detail message.
     */
    public MimeTypeParseException(String s) {
	super(s);
    }
}
