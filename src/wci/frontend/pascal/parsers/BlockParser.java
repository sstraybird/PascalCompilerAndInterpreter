package wci.frontend.pascal.parsers;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;

import static wci.frontend.pascal.PascalTokenType.BEGIN;

/**
 * <h1>BlockParser</h1>
 * <p>Parse a Pascal block</p>
 */
public class BlockParser extends PascalParserTD {

    /**
     * Constructor
     * @param parent the parent parser
     */
    public BlockParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * Parse a block
     * @param token the initial token.
     * @param routineId the symbol table entry of the routine name.
     * @return the root node of the parse tree.
     * @throws Exception if an error occurred.
     */
    public ICodeNode parse(Token token, SymTabEntry routineId) throws Exception{
        DeclarationsParser declarationsParser = new DeclarationsParser(this);
        StatementParser statementParser = new StatementParser(this);

        // Parse any declarations
        declarationsParser.parse(token,routineId);

        token = synchronize(StatementParser.STMT_START_SET);
        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        // Look for the BEGIN token to parse a compound statement.
        if(tokenType == BEGIN){
            rootNode = statementParser.parse(token);
        }
        //Missing BEGIN:Attempt to parse anyway if possible.
        else {
            errorHandler.flag(token, PascalErrorCode.MISSING_BEGIN,this);
            if(StatementParser.STMT_START_SET.contains(tokenType));
            statementParser.parseList(token,rootNode, PascalTokenType.END,PascalErrorCode.MISSING_END);
        }

        return rootNode;
    }
}
