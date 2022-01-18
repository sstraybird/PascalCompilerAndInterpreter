package wci.frontend.pascal;

import wci.frontend.EofToken;
import wci.frontend.Parser;
import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.message.Message;
import wci.message.MessageType;

/**
 * <h1>PascalParserTD</h1>
 *
 * <p>The top-down Pascal Parser</p>
 */
public class PascalParserTD extends Parser {
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

        while (!((token = nextToken()) instanceof EofToken)){}

        // Send the parser summary message
        float elapsedTime = (System.currentTimeMillis() - startTime)/1000f ;
        sendMessage(new Message(MessageType.PARSER_SUMARY,new Number[]{token.getLineNumber(),getErrorCount(),elapsedTime}));
    }

    /**
     * Return the number of syntax errors found by the parser
     * @return the error count.
     */
    @Override
    public int getErrorCount() {
        return 0;
    }
}
