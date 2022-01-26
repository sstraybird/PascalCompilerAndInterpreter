package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.parsers.StatementParser;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageType;

import java.io.IOException;

import static wci.frontend.pascal.PascalErrorCode.MISSING_PERIOD;
import static wci.frontend.pascal.PascalErrorCode.UNEXPECTED_TOKEN;
import static wci.frontend.pascal.PascalTokenType.*;

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
     * Constructor for subclasses.
     * @param parent the parent parser.
     */
    public PascalParserTD(PascalParserTD parent){
        super(parent.getScanner());
    }
    /**
     * Parse a pascal source program and generate the symbol table and the intermediate code
     * @throws Exception if an error occurred.
     */
    @Override
    public void parse() throws Exception {
        long startTime = System.currentTimeMillis();
        iCode = ICodeFactory.createICode();


        try{
            Token token = nextToken();
            ICodeNode rootNode = null ;
            //Look for the BEGIN token to parse a compound statement.
            if(token.getType() == BEGIN){
                StatementParser statementParser = new StatementParser(this);
                rootNode = statementParser.parse(token);
                token = currentToken();
            }else {
                errorHandler.flag(token,UNEXPECTED_TOKEN,this);
            }

            // Look for the final period.
            if(token.getType() != DOT){
                errorHandler.flag(token,MISSING_PERIOD,this);
            }
            token = currentToken();

            // Set the parse tree root node.
            if(rootNode!=null){
                iCode.setRoot(rootNode);
            }

            // Send the parser summary message
            float elapsedTime = (System.currentTimeMillis() - startTime)/1000f ;
            sendMessage(new Message(MessageType.PARSER_SUMARY,new Number[]{token.getLineNumber(),
                                                                            getErrorCount(),
                                                                            elapsedTime}));
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
