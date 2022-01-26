package wci.frontend.pascal.parsers;

import wci.frontend.EofToken;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;

import static wci.frontend.pascal.PascalTokenType.IDENTIFIER;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.NO_OP;

public class StatementParser extends PascalParserTD {

    /**
     * Constructor
     * @param parent the parent parser.
     */
    public StatementParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * Parse a statement
     * To be overriden by the specialized statement parser subclasses.
     * @param token the initial token
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ICodeNode parse(Token token) throws Exception{
        ICodeNode statementNode = null;
        switch ((PascalTokenType)token.getType()){
            case BEGIN: {
                CompundStatementParser compundStatementParser = new CompundStatementParser(this);
                statementNode = compundStatementParser.parse(token);
                break;
            }
            //An assignment statement begins with a variable's identifier.
            case IDENTIFIER:{
                AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
                statementNode = assignmentParser.parse(token);
                break;
            }
            default:{
                statementNode = ICodeFactory.createICodeNode(NO_OP);
                break;
            }
        }
        // Set the current line number as an attribute.
        setLineNumber(statementNode,token);
        return statementNode;
    }

    /**
     * Set the current line number as a statement node attribute.
     * @param node iCodeNode
     * @param token Token
     */
    protected void setLineNumber(ICodeNode node,Token token){
        if(node != null){
            node.setAttribute(LINE,token.getLineNumber());
        }
    }

    /**
     * Parse a statement list
     * @param token the current token
     * @param parentNode the parent node of the statement list
     * @param terminator the token type of the node that terminates  the list
     * @param errorCode the error code if the terminator token is missing
     * @throws Exception if an error occurred.
     */
    protected void parseList(Token token, ICodeNode parentNode, PascalTokenType terminator, PascalErrorCode errorCode) throws Exception{
        // Loop to parse each statement until the END token or the end of the source file.
        while (!(token instanceof EofToken) && (token.getType() != terminator)){
            // Parse a statement. The parent node adopts the statement node.
            ICodeNode statementNode = parse(token);
            parentNode.addChild(statementNode);

            token = currentToken();
            TokenType tokenType = token.getType();

            // Look for the semicolon between statements.
            if(tokenType == SEMICOLON){
                token = nextToken();        //cosume the  ;
            }
            // if at the start of the next assignment statement,then missing a semicolon.
            else if(tokenType == IDENTIFIER){
                errorHandler.flag(token,PascalErrorCode.MISSING_SEMICOLON,this);
            }
            // Unexpected token.
            else if(tokenType != terminator){
                errorHandler.flag(token,PascalErrorCode.UNEXPECTED_TOKEN,this);
                token = nextToken();        //consume the unexpected token.
            }
        }

        // Look for the terminator token.
        if(token.getType() == terminator){
            token = nextToken();            //consume the terminator token
        }else {
            errorHandler.flag(token,errorCode,this);
        }
    }
}