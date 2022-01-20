package wci.frontend.pascal;

import wci.frontend.*;
import wci.message.Message;
import wci.message.MessageType;

import java.io.IOException;

import static wci.frontend.pascal.PascalTokenType.ERROR;

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

                if(tokenType != ERROR){
                    //Format each token
                    sendMessage(new Message(MessageType.TOKEN,new Object[]{token.getLineNumber(),
                            token.getPosition(),
                            tokenType,
                            token.getText(),
                            token.getValue()}));
                }else {
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
