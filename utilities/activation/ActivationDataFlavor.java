package ADaMSoft.utilities.activation;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;


public class ActivationDataFlavor extends DataFlavor {

    private String mimeType = null;
    private MimeType mimeObject = null;
    private String humanPresentableName = null;
    private Class representationClass = null;
    public ActivationDataFlavor(Class representationClass,
		      String mimeType, String humanPresentableName) {
	super(mimeType, humanPresentableName); // need to call super

	// init private variables:
	this.mimeType = mimeType;
	this.humanPresentableName = humanPresentableName;
	this.representationClass = representationClass;
    }

    /**
     * Construct a DataFlavor that represents a MimeType.
     * <p>
     * The returned DataFlavor will have the following characteristics:
     * <p>
     * If the mimeType is "application/x-java-serialized-object;
     * class=", the result is the same as calling new
     * DataFlavor(Class.forName()) as above.
     * <p>
     * otherwise:
     * <p>
     * representationClass = InputStream<p>
     * mimeType = mimeType<p>
     *
     * @param representationClass the class used in this DataFlavor
     * @param humanPresentableName the human presentable name of the flavor
     */
    public ActivationDataFlavor(Class representationClass,
				String humanPresentableName) {
	super(representationClass, humanPresentableName);
	this.mimeType = super.getMimeType();
	this.representationClass = representationClass;
      	this.humanPresentableName = humanPresentableName;
    }

    /**
     * Construct a DataFlavor that represents a MimeType.
     * <p>
     * The returned DataFlavor will have the following characteristics:
     * <p>
     * If the mimeType is "application/x-java-serialized-object; class=",
     * the result is the same as calling new DataFlavor(Class.forName()) as
     * above, otherwise:
     * <p>
     * representationClass = InputStream<p>
     * mimeType = mimeType
     *
     * @param mimeType the MIME type of the data represented by this class
     * @param humanPresentableName the human presentable name of the flavor
     */
    public ActivationDataFlavor(String mimeType, String humanPresentableName) {
	super(mimeType, humanPresentableName);
	this.mimeType = mimeType;
	try {
	    this.representationClass = Class.forName("java.io.InputStream");
	} catch (ClassNotFoundException ex) {
	    // XXX - should never happen, ignore it
	}
      	this.humanPresentableName = humanPresentableName;
    }

    /**
     * Return the MIME type for this DataFlavor.
     *
     * @return	the MIME type
     */
    public String getMimeType() {
	return mimeType;
    }

    /**
     * Return the representation class.
     *
     * @return	the representation class
     */
    public Class getRepresentationClass() {
	return representationClass;
    }

    /**
     * Return the Human Presentable name.
     *
     * @return	the human presentable name
     */
    public String getHumanPresentableName() {
	return humanPresentableName;
    }

    /**
     * Set the human presentable name.
     *
     * @param humanPresentableName	the name to set
     */
    public void setHumanPresentableName(String humanPresentableName) {
	this.humanPresentableName = humanPresentableName;
    }

    /**
     * Compares the DataFlavor passed in with this DataFlavor; calls
     * the <code>isMimeTypeEqual</code> method.
     *
     * @param dataFlavor	the DataFlavor to compare with
     * @return			true if the MIME type and representation class
     *				are the same
     */
    public boolean equals(DataFlavor dataFlavor) {
	return (isMimeTypeEqual(dataFlavor) &&
	 	dataFlavor.getRepresentationClass() == representationClass);
    }

    /**
     * Is the string representation of the MIME type passed in equivalent
     * to the MIME type of this DataFlavor. <p>
     *
     * ActivationDataFlavor delegates the comparison of MIME types to
     * the MimeType class included as part of the JavaBeans Activation
     * Framework. This provides a more robust comparison than is normally
     * available in the DataFlavor class.
     *
     * @param mimeType	the MIME type
     * @return		true if the same MIME type
     */
    public boolean isMimeTypeEqual(String mimeType) {
	MimeType mt = null;
	try {
	    if (mimeObject == null)
		mimeObject = new MimeType(this.mimeType);
	    mt = new MimeType(mimeType);
	} catch (MimeTypeParseException e) {
	    // something didn't parse, do a crude comparison
	    return this.mimeType.equalsIgnoreCase(mimeType);
	}

	return mimeObject.match(mt);
    }

    /**
     * Called on DataFlavor for every MIME Type parameter to allow DataFlavor
     * subclasses to handle special parameters like the text/plain charset
     * parameters, whose values are case insensitive.  (MIME type parameter
     * values are supposed to be case sensitive).
     * <p>
     * This method is called for each parameter name/value pair and should
     * return the normalized representation of the parameterValue.
     * This method is never invoked by this implementation.
     *
     * @param parameterName	the parameter name
     * @param parameterValue	the parameter value
     * @return			the normalized parameter value
     * @deprecated
     */
    protected String normalizeMimeTypeParameter(String parameterName,
						String parameterValue) {
	return parameterValue;
    }

    /**
     * Called for each MIME type string to give DataFlavor subtypes the
     * opportunity to change how the normalization of MIME types is
     * accomplished.
     * One possible use would be to add default parameter/value pairs in cases
     * where none are present in the MIME type string passed in.
     * This method is never invoked by this implementation.
     *
     * @param mimeType	the MIME type
     * @return		the normalized MIME type
     * @deprecated
     */
    protected String normalizeMimeType(String mimeType) {
	return mimeType;
    }
}
