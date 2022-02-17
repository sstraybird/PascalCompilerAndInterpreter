package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.parsers.BlockParser;
import wci.frontend.pascal.parsers.ProgramParser;
import wci.frontend.pascal.parsers.StatementParser;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.message.Message;
import wci.message.MessageType;

import java.io.IOException;
import java.util.EnumSet;

import static wci.frontend.pascal.PascalErrorCode.MISSING_PERIOD;
import static wci.frontend.pascal.PascalErrorCode.UNEXPECTED_TOKEN;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_ICODE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_SYMTAB;

/**
 * <h1>PascalParserTD</h1>
 *
 * <p>The top-down Pascal Parser</p>
 */
public class PascalParserTD extends Parser {

    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();

    private SymTabEntry routineId;      // name of the routine being parsed
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

        ICode iCode = ICodeFactory.createICode();
        Predefined.initialize(symTabStack);

        try{
            Token token = nextToken();

            // Parse a program
            ProgramParser programParser = new ProgramParser(this);
            programParser.parse(token,null);

            token = currentToken();

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

    /**
     * Synchronize the parser
     * @param syncSet the set of token types for synchronizing the parser
     * @return the token where the parser has synchronized.
     * @throws Exception if an error occurred.
     */
    public Token synchronize(EnumSet syncSet) throws Exception{
        Token token = currentToken();

        // If the current token is not in the synchronization set.
        // then it is unexpected and the parser must recover
        if(!syncSet.contains(token.getType())){
            //Flag the unexpected token.
            errorHandler.flag(token,UNEXPECTED_TOKEN,this);

            //Recover by skipping tokens that are not in the synchronization set
            do {
                token = nextToken();
            }while (!(token instanceof EofToken) && !syncSet.contains(token.getType()));
        }
        return token;
    }
}
