package wci.frontend.pascal;

/**
 * <h1>PascalErrorCode</h1>
 *
 * <p>Pascal translation error codes</p>
 */
public enum PascalErrorCode {
    ALREADY_FORWARDED("Already specified in FORWARD"),
    CASE_CONSTANT_REUSED("CASE constant reused"),
    IDENTIFIER_REDEFINED("Redefined identifier"),
    IDENTIFIER_UNDEFINED("Undefined identifier"),
    INCOMPATIBLE_ASSIGNMENT("Incompatible assignment"),
    INCOMPATIBLE_TYPES("Incompatible types"),
    INVALID_ASSIGNMENT("Invalid assignment statement"),
    INVALID_CHARACTER("Invalid character"),
    INVALID_CONSTANT("Invalid constant"),
    INVALID_EXPONENT("Invalid exponent"),
    INVALID_EXPRESSION("Invalid expression"),
    INVALID_FIELD("Invalid field"),
    INVALID_FRACTION("Invalid fraction"),
    INVALID_IDENTIFIER_USAGE("Invalid identifier usage"),
    INVALID_INDEX_TYPE("Invalid index type"),
    INVALID_NUMBER("Invalid number"),

    NOT_CONSTANT_IDENTIFIER("Not a constant identifier"),
    NOT_RECORD_VARIABLE("Not a record variable"),
    NOT_TYPE_IDENTIFIER("Not a type identifier"),
    RANGE_INTEGER("Integer literal out of range"),
    RANGE_REAL("Real literal out of range"),



    UNEXPECTED_EOF("Unexpected end of file"),
    //Fatal errors.
    IO_ERROR(-101,"Object I/O error"),
    TOO_MANY_ERRORS(-102,"Too many syntax errors")
    ;



    private int status;         //text status
    private String message;     //error message
    /**
     * Contructor
     * @param message the error message.
     */
    PascalErrorCode(String message) {
        this.status = 0;
        this.message = message;
    }

    /**
     * Constructor.
     * @param status the exit status
     * @param message the error message
     */
    PascalErrorCode(int status,String message){
        this.status = status;
        this.message = message;
    }

    /**
     * Getter
     * @return the exit status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Getter
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return the message
     */
    @Override
    public String toString() {
       return message;
    }
}
