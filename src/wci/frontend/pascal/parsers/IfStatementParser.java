package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;

import javax.swing.plaf.nimbus.State;
import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.ELSE;
import static wci.frontend.pascal.PascalTokenType.THEN;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.IF;

/**
 * <h1>IfStatementParser</h1>
 * <p>Parse a Pascal IF statement</p>
 */
public class IfStatementParser extends StatementParser{
    /**
     * Constructor
     *
     * @param parent the parent parser.
     */
    public IfStatementParser(PascalParserTD parent) {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> THEN_SET = StatementParser.STMT_START_SET.clone();
    static {
        THEN_SET.add(THEN);
        THEN_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    /**
     * Parse an IF statement
     * @param token the initial token
     * @return the root node of the generated parse tree
     * @throws Exception if an error occurred.
     */
    @Override
    public ICodeNode parse(Token token) throws Exception {
        token = nextToken();            //consume the IF

        // Create an IF node
        ICodeNode ifNode = ICodeFactory.createICodeNode(IF);

        // parse the expression.
        // The IF node adopts the expression subtree as its first child
        ExpressionParser expressionParser = new ExpressionParser(this);
        ifNode.addChild(expressionParser.parse(token));

        //Synchronize at the THEN
        token = synchronize(THEN_SET);
        if(token.getType() == THEN){
            token = nextToken();        // consume the THEN
        }else {
            errorHandler.flag(token, PascalErrorCode.MISSING_THEN,this);
        }

        // Parse the THEN statement
        // The IF node adopts the statement subtree as tis second child.
        StatementParser statementParser = new StatementParser(this);
        ifNode.addChild(statementParser.parse(token));
        token = currentToken();

        //Look for an ELSE
        if(token.getType() == ELSE){
            token = nextToken();        //consume the THEN
            //parse the ELSE statement
            //The IF node adopts the statement subtree as its third child.
            ifNode.addChild(statementParser.parse(token));
        }
        return ifNode;
    }
}
