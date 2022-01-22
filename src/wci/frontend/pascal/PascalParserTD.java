package wci.frontend.pascal;

import wci.frontend.*;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageType;

import java.io.IOException;

import static wci.frontend.pascal.PascalTokenType.ERROR;
import static wci.frontend.pascal.PascalTokenType.IDENTIFIER;

/**
 * <h1>PascalParserTD</h1>
 *
 * <p>The top-down Pascal Parser</p>
 */
public class PascalParserTD extends Parser {

    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();
    /**
     * Constructor
     * @param scanner the scanner to be used with this parser
     */
    public PascalParserTD(Scanner scanner) {
        super(scanner);
    }

    /**
     * Parse a pascal source program and generate the symbol table and the intermediate code
     * @throws Exception
     */
    @Override
    public void parse() throws Exception {
        Token token;
        long startTime = System.currentTimeMillis();

        try{
            while (!((token = nextToken()) instanceof EofToken)){
                TokenType tokenType = token.getType();
                // Cross reference only the identifiers.
                if(tokenType == IDENTIFIER){
                    String name = token.getText().toLowerCase();
                    // If it's not already in the symbol table,create and enter a new entry for the identifier.
                    SymTabEntry entry = symTabStack.lookup(name);
                    if (entry == null){
                        entry = symTabStack.enterLocal(name);
                    }
                    // Append the current line number to the entry
                    entry.appendLineNumber(token.getLineNumber());
                }else if(tokenType == ERROR){
                    errorHandler.flag(token,(PascalErrorCode)token.getValue(),this);
                }

            }

            // Send the parser summary message
            float elapsedTime = (System.currentTimeMillis() - startTime)/1000f ;
            sendMessage(new Message(MessageType.PARSER_SUMARY,new Number[]{token.getLineNumber(),getErrorCount(),elapsedTime}));
        }catch (IOException ex){
            errorHandler.abortTranslation(PascalErrorCode.IO_ERROR,this);
        }
    }

    /**
     * Return the number of syntax errors found by the parser
     * @return the error count.
     */
    @Override
    public int getErrorCount() {
        return errorHandler.getErrorCount();
    }
}
