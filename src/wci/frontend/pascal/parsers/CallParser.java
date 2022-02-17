package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

import java.util.ArrayList;
import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.symtabimpl.DefinitionImpl.VAR_PARM;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.DECLARED;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.FORWARD;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_PARMS;
import static wci.intermediate.typeimpl.TypeFormImpl.SCALAR;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;

/**
 * <h1>CallParser</h1>
 * <p>Parse a called to a procedure or function.</p>
 */
public class CallParser extends StatementParser{
    /**
     * Constructor
     *
     * @param parent the parent parser.
     */
    public CallParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * Parse a call to a declared procedure or function
     * @param token the initial token
     * @return the root node of the generated parse tree
     * @throws Exception if an error occurred.
     */
    @Override
    public ICodeNode parse(Token token) throws Exception {
        SymTabEntry pfId = symTabStack.lookup(token.getText().toLowerCase());
        RoutineCode routineCode = (RoutineCode) pfId.getAttribute(ROUTINE_CODE);
        StatementParser callParser = (routineCode == DECLARED) || (routineCode == FORWARD)
                ? new CallDeclaredParser(this):new CallStandardParser(this);
        return callParser.parse(token);
    }

    // Synchronization set for the , token.
    private static final EnumSet<PascalTokenType> COMMA_SET =
            ExpressionParser.EXPR_START_SET.clone();
    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(RIGHT_PAREN);
    };
    /**
     * Parse the acutal parameters of a procedure or function call
     * @param token the current token.
     * @param pfId the symbol table entry of the procedure or function name
     * @param isDeclared true if parsing actual parms of a declared routine
     * @param isReadReadln true if parsing actual parms of read or readln
     * @param isWriteWriteln true if parsing actual parms of write or writeln.
     * @return the PARAMETERS node,or null if there are no actual parameters.
     * @throws Exception if an error occurred.
     */
    protected ICodeNode parseActualParameters(Token token,SymTabEntry pfId,
                                              boolean isDeclared,
                                              boolean isReadReadln,
                                              boolean isWriteWriteln) throws Exception{
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode parmsNode = ICodeFactory.createICodeNode(PARAMETERS) ;
        ArrayList<SymTabEntry> formalparms = null;
        int parmCount = 0;
        int parmIndex = -1;

        if(isDeclared){
            formalparms = (ArrayList<SymTabEntry>)pfId.getAttribute(ROUTINE_PARMS);
            parmCount = formalparms != null ? formalparms.size() : 0;
        }

        if(token.getType() != LEFT_PAREN){
            if(parmCount != 0){
                errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARMS,this);
            }
            return null;
        }

        token = nextToken();        //consume opening (

        // Loop to parse each actual parameter.
        while (token.getType() != RIGHT_PAREN){
            ICodeNode actualNode = expressionParser.parse(token);

            // Declared procedure or function: Check the number of actual parameters,
            // and check each actual parameter against the corresponding formal parameter.
            if(isDeclared){
                if(++parmIndex<parmCount){
                    SymTabEntry formalId = formalparms.get(parmIndex);
                    checkActualParameter(token,formalId,actualNode);
                }else if(parmIndex == parmCount){
                    errorHandler.flag(token,PascalErrorCode.WRONG_NUMBER_OF_PARMS,this);
                }
            }
            // read or readln:
            // Each actual parameter must be a variable that is a scalar,boolean,or subrange of integer
            else if(isReadReadln){
                TypeSpec type = actualNode.getTypeSpec();
                TypeForm form = type.getForm();
                if(!((actualNode.getType() == ICodeNodeTypeImpl.VARIABLE)
                        && ((form == SCALAR) || (type == Predefined.booleanType) || ( (form == SUBRANGE) && (type.baseType() == Predefined.integerType) )))) {
                    errorHandler.flag(token,PascalErrorCode.INVALID_VAR_PARM,this);
                }
            }
            // write or writeln:The type of each actual parameters must be a scalar,boolean,or a Pascal string.
            // Parse any field width and precision
            else if(isWriteWriteln){
                // Create a WRITE_PARM  node which adopts the expression node.
                ICodeNode exprNode = actualNode;
                actualNode = ICodeFactory.createICodeNode(WRITE_PARM);
                actualNode.addChild(exprNode);

                TypeSpec type = exprNode.getTypeSpec().baseType();
                TypeForm form = type.getForm();

                if(!((form == SCALAR) || (type == Predefined.booleanType) || (type.isPascalString()))){
                    errorHandler.flag(token,PascalErrorCode.INCOMPATIBLE_TYPES,this);
                }

                // Optional field width
                token = currentToken();
                actualNode.addChild(parseWriteSpec(token));

                // Optional precision.
                token = currentToken();
                actualNode.addChild(parseWriteSpec(token));
            }
            parmsNode.addChild(actualNode);
            token = synchronize(COMMA_SET) ;
            TokenType tokenType = token.getType();

            // Look for the comma
            if(tokenType == COMMA){
                token = nextToken();        //consume ,
            }else if(ExpressionParser.EXPR_START_SET.contains(tokenType)){
                errorHandler.flag(token,PascalErrorCode.MISSING_COMMA,this);
            }else if(tokenType != RIGHT_PAREN){
                token = synchronize(ExpressionParser.EXPR_START_SET);
            }
        }

        token = nextToken();        // consume closing )

        if((parmsNode.getChildren().size() == 0) || (isDeclared && (parmIndex != parmCount -1))){
            errorHandler.flag(token,PascalErrorCode.WRONG_NUMBER_OF_PARMS,this);
        }
        return parmsNode;
    }

    /**
     * Parse the field width or precision for an actual parameter
     * @param token
     * @return
     * @throws Exception
     */
    private ICodeNode parseWriteSpec(Token token) throws Exception{
        if(token.getType() == COLON){
            token = nextToken();        //consume :

            ExpressionParser expressionParser = new ExpressionParser((this));
            ICodeNode specNode = expressionParser.parse(token);

            if(specNode.getType() == INTEGER_CONSTANT){
                return specNode;
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_NUMBER,this);
                return null;
            }
        }else {
            return null;
        }
    }

    /**
     * Check an acutal parameter against the corresponding formal parameter.
     * @param token
     * @param formalId
     * @param actualNode
     */
    private void checkActualParameter(Token token, SymTabEntry formalId, ICodeNode actualNode) {
        Definition formalDefn = formalId.getDefinition();
        TypeSpec formalType = formalId.getTypeSpec();
        TypeSpec actualType = actualNode.getTypeSpec();

        // VAR parameter: The actual parameter must be a variable of the same type as the formal parameter
        if(formalDefn == VAR_PARM){
            if((actualNode.getType() != VARIABLE) || (actualType != formalType)){
                errorHandler.flag(token,PascalErrorCode.INVALID_VAR_PARM,this);
            }
        }
        // Value parameter: The actual parameter must be assignment-compatible with the formal parameter.
        else if(!TypeChecker.areAssignmentCompatible(formalType,actualType)){
            errorHandler.flag(token,PascalErrorCode.INCOMPATIBLE_TYPES,this);
        }
    }
}
