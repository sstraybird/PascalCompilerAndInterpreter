package wci.frontend.pascal.parsers;

import com.sun.corba.se.impl.corba.TypeCodeImplHelper;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;
import wci.intermediate.typeimpl.TypeFormImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SUBSCRIPTS;
import static wci.intermediate.symtabimpl.DefinitionImpl.*;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

public class VariableParser extends StatementParser{
    // Set to true to parse a function name
    // as the target of an assignment.
    private boolean isFunctionTarget = false;
    /**
     * Constructor
     *
     * @param parent the parent parser.
     */
    public VariableParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * Parse a variable
     * @param token the initial token
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    @Override
    public ICodeNode parse(Token token) throws Exception {
        // Look up the identifier in the symbol table stack.
        String name = token.getText().toLowerCase();
        SymTabEntry variableId = symTabStack.lookup(name);

        // If not found,flag the error and enter the identifier
        // as an undefined identifier with an undefined type.
        if(variableId == null){
            errorHandler.flag(token, PascalErrorCode.IDENTIFIER_UNDEFINED,this);
            variableId = symTabStack.enterLocal(name);
            variableId.setDefinition(UNDEFINED);
            variableId.setTypeSpec(Predefined.undefinedType);
        }
        return parse(token,variableId);
    }
    // Synchronization set to start a subscript or a field.
    private static final EnumSet<PascalTokenType> SUBSCRIPT_FIELD_START_SET =
            EnumSet.of(LEFT_BRACKET, DOT);
    /**
     * Parse a variable
     * @param token the initial token.
     * @param variableId the symbol table entry of the variable identifier.
     * @return
     * @throws Exception
     */
    public ICodeNode parse(Token token,SymTabEntry variableId) throws Exception{
        //Check how the variable is defined.
        Definition defnCode = variableId.getDefinition();
        if(!((defnCode == VARIABLE)
                || (defnCode == VALUE_PARM)
                || (defnCode == VAR_PARM)
        || (isFunctionTarget &&(defnCode == DefinitionImpl.FUNCTION)))) {
            errorHandler.flag(token, PascalErrorCode.INVALID_IDENTIFIER_USAGE, this);
        }
        variableId.appendLineNumber(token.getLineNumber());

        ICodeNode variableNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
        variableNode.setAttribute(ID,variableId);

        token = nextToken();        //consume the identifier.

        // Parse array subscripts or record fields.
        TypeSpec variableType = variableId.getTypeSpec();
        if(!isFunctionTarget) {
            while (SUBSCRIPT_FIELD_START_SET.contains(token.getType())) {
                ICodeNode subFldNode = token.getType() == LEFT_BRACKET
                        ? parseSubscripts(variableType) : parseField(variableType);
                token = currentToken();

                // Update the variable's type.
                // The variable node adopts the SUBSCRIPTS or FIELD node.
                variableType = subFldNode.getTypeSpec();
                variableNode.addChild(subFldNode);
            }
        }
        variableNode.setTypeSpec(variableType);
        return variableNode;
    }

    /**
     * Parse a record field.
     * @param variableType the type of the record variable.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseField(TypeSpec variableType) throws Exception{
        // Create a FIELD node.
        ICodeNode fieldNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.FIELD);

        Token token = nextToken();      //consume the . token
        TokenType tokenType = token.getType();
        TypeForm variableForm = variableType.getForm();

        if((tokenType == IDENTIFIER) && (variableForm == TypeFormImpl.RECORD)){
            SymTab symTab = (SymTab) variableType.getAttribute(RECORD_SYMTAB);
            String fieldName = token.getText().toLowerCase();
            SymTabEntry fieldId = symTab.lookup(fieldName);

            if(fieldId != null){
                variableType = fieldId.getTypeSpec();
                fieldId.appendLineNumber(token.getLineNumber());

                // Set the field identifier's name.
                fieldNode.setAttribute(ID,fieldId);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_FIELD,this);
            }
        }else {
            errorHandler.flag(token,PascalErrorCode.INVALID_FIELD,this);
        }
        token = nextToken();        // consume the field identifier

        fieldNode.setTypeSpec(variableType);
        return fieldNode;
    }

    // Synchronization set for the ] token.
    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET =
            EnumSet.of(RIGHT_BRACKET, EQUALS, SEMICOLON);

    /**
     * Parse a set of comma-separated subscript expressions.
     * @param variableType the type of the array variable.
     * @return the root node  of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseSubscripts(TypeSpec variableType) throws Exception{
        Token token ;
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode subscriptsNode = ICodeFactory.createICodeNode(SUBSCRIPTS);

        do{
            token = nextToken();        //consume the [ or , token

            // The current variable is an array
            if(variableType.getForm() == TypeFormImpl.ARRAY){
                // Parse the subscript expression.
                ICodeNode exprNode = expressionParser.parse(token);
                TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec():Predefined.undefinedType;

                // The subscript expression type must be assignment compatible with the array index type.
                TypeSpec indexType = (TypeSpec) variableType.getAttribute(ARRAY_INDEX_TYPE);
                if(!TypeChecker.areAssignmentCompatible(indexType,exprType)){
                    errorHandler.flag(token,PascalErrorCode.INCOMPATIBLE_TYPES,this);
                }

                // The SUBSCRIPTS node adopts the subscript expression tree.
                subscriptsNode.addChild(exprNode);

                // Update the variable's type.
                variableType = (TypeSpec) variableType.getAttribute(ARRAY_ELEMENT_TYPE);
            }
            // Not an array type,so too many subscripts.
            else {
                errorHandler.flag(token,PascalErrorCode.TOO_MANY_SUBSCRIPTS,this);
                expressionParser.parse(token);
            }
            token = currentToken();
        }while (token.getType() == COMMA) ;

        // Synchronize at the ] token.
        token = synchronize(RIGHT_BRACKET_SET);
        if(token.getType() == RIGHT_BRACKET){
            token  = nextToken();       //consume the ] token
        }else {
            errorHandler.flag(token,PascalErrorCode.MISSING_RIGHT_BRACKET,this);
        }
        subscriptsNode.setTypeSpec(variableType);
        return subscriptsNode;
    }

    public ICodeNode parseFunctionNameTarget(Token token) throws Exception{
        isFunctionTarget = true;
        return parse(token);
    }
}

