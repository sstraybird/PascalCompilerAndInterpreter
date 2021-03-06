package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.RoutineCodeImpl;

import java.util.prefs.PreferenceChangeEvent;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.CALL;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;

public class CallStandardParser extends CallParser{
    /**
     * Constructor
     *
     * @param parent the parent parser.
     */
    public CallStandardParser(PascalParserTD parent) {
        super(parent);
    }

    /**
     * Parse a call to a declared procedure or function
     * @param token the initial token
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    @Override
    public ICodeNode parse(Token token) throws Exception {
        ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
        SymTabEntry pfId = symTabStack.lookup(token.getText().toLowerCase());
        RoutineCode routineCode = (RoutineCode)pfId.getAttribute(ROUTINE_CODE);
        callNode.setAttribute(ID,pfId);

        token = nextToken();        //consume procedure or function identifier.
        switch ((RoutineCodeImpl)routineCode){
            case READ:
            case READLN: return parseReadReadln(token,callNode,pfId);

            case WRITE:
            case WRITELN: return parseWriteWriteln(token,callNode,pfId);

            case EOF:
            case EOLN: return parseEofEoln(token,callNode,pfId);

            case ARCTAN:
            case COS:
            case EXP:
            case LN:
            case SIN:
            case SQRT: return parseArctanCosExpLnSinSqrt(token,callNode,pfId);

            case PRED:
            case SUCC: return parsePredSucc(token,callNode,pfId);

            case CHR: return parseChr(token,callNode,pfId);
            case ODD: return parseOdd(token,callNode,pfId);
            case ORD: return parseOrd(token,callNode,pfId);

            case ROUND:
            case TRUNC: return parseRoundTrunc(token,callNode,pfId);

            default:return null;            //should never get here.
        }
    }

    /**
     * Parse a call to round or trunc
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseRoundTrunc(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        // Parse any actual parameters
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be one real parameter.
        // The function return type is integer.
        if(checkParmCount(token,parmsNode,1)){
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if(argType == Predefined.realType){
                callNode.setTypeSpec(Predefined.integerType);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_TYPE,this);
            }
        }
        return callNode;
    }

    /**
     * Parse a call to ord
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseOrd(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        // Parse any actual parameters
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be one character or enumeration parameter.
        // The function return type is integer
        if(checkParmCount(token,parmsNode,1)){
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();

            if((argType == Predefined.charType) || (argType.getForm() == ENUMERATION)){
                callNode.setTypeSpec(Predefined.integerType);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_TYPE,this);
            }
        }
        return callNode;
    }

    /**
     * Parse a call to odd.
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseOdd(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        // Parse any actual parameters.
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be one integer parameter.
        // The function return type is boolean.
        if(checkParmCount(token,parmsNode,1)){
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if(argType == Predefined.integerType){
                callNode.setTypeSpec(Predefined.booleanType);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_TYPE,this);
            }
        }
        return callNode;
    }

    /**
     * Parse a call to chr
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseChr(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        //Parse any actual parameters.
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be one integer parameter.
        // The function return type is character.
        if(checkParmCount(token,parmsNode,1)){
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if(argType == Predefined.integerType){
                callNode.setTypeSpec(Predefined.charType);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_TYPE,this);
            }
        }
        return callNode;
    }

    /**
     * Parse a call to pred or succ
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parsePredSucc(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        // Parse any actual parameters
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be one integer or enumeration parameter.
        // The function return type is the parameter type.
        if(checkParmCount(token,parmsNode,1)){
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();
            if((argType == Predefined.integerType) || (argType.getForm() == ENUMERATION)){
                callNode.setTypeSpec(argType);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_TYPE,this);
            }
        }
        return callNode;
    }

    /**
     * Parse a call to arctan,cos,exp,ln,sin,or sqrt.
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseArctanCosExpLnSinSqrt(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be one integer or real parameter
        //The function return type is real
        if(checkParmCount(token,parmsNode,1)){
            TypeSpec argType = parmsNode.getChildren().get(0).getTypeSpec().baseType();

            if(argType == Predefined.integerType || (argType == Predefined.realType)){
                callNode.setTypeSpec(Predefined.realType);
            }else {
                errorHandler.flag(token,PascalErrorCode.INVALID_TYPE,this);
            }
        }
        return callNode;
    }

    /**
     * Parse a call to eof or eoln
     * @param token the current token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseEofEoln(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        // Parse any actual parameters
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,false);
        callNode.addChild(parmsNode);

        // There should be no actual parameters
        if(checkParmCount(token,parmsNode,0)){
            callNode.setTypeSpec(Predefined.booleanType);
        }
        return callNode;
    }

    /**
     * Parse a call to write or writeln.
     * @param token the current token.
     * @param callNode the CALL node.
     * @param pfId the symbol table entry of the standard routine name.
     * @return ICodeNode the CALL node
     * @throws Exception if an error occurred
     */
    private ICodeNode parseWriteWriteln(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception {
        // Parse any actual parameters
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,false,true);
        callNode.addChild(parmsNode);

        // Write must have parameters
        if((pfId == Predefined.writeId) && (callNode.getChildren().size() == 0)){
            errorHandler.flag(token,PascalErrorCode.WRONG_NUMBER_OF_PARMS,this);
        }
        return callNode;
    }

    /**
     * Parse a call to read or readln
     * @param token
     * @param callNode
     * @param pfId
     * @return
     * @throws Exception
     */
    private ICodeNode parseReadReadln(Token token, ICodeNode callNode, SymTabEntry pfId) throws Exception{
        // Parse any actual parameters.
        ICodeNode parmsNode = parseActualParameters(token,pfId,false,true,false);
        callNode.addChild(parmsNode);

        // Read must have parameters.
        if((pfId == Predefined.realId) && (callNode.getChildren().size() ==0)){
            errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARMS,this);
        }
        return callNode;
    }

    private boolean checkParmCount(Token token,ICodeNode parmsNode,int count){
        if(((parmsNode == null) && (count == 0)) || (parmsNode.getChildren().size() == count)){
            return true;
        }else {
            errorHandler.flag(token,PascalErrorCode.WRONG_NUMBER_OF_PARMS,this);
            return false;
        }
    }
}
