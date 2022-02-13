package wci.frontend.pascal.parsers;


import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.END;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.symtabimpl.DefinitionImpl.FIELD;
import static wci.intermediate.typeimpl.TypeFormImpl.RECORD;
import static wci.intermediate.typeimpl.TypeKeyImpl.RECORD_SYMTAB;

public class RecordTypeParser extends TypeSpecificationParser {
    public RecordTypeParser(PascalParserTD parent) {
        super(parent);
    }

    // Synchronization set for the END.
    private static final EnumSet<PascalTokenType> END_SET =
            DeclarationsParser.VAR_START_SET.clone();
    static {
        END_SET.add(END);
        END_SET.add(SEMICOLON);
    }

    @Override
    public TypeSpec parse(Token token) throws Exception {
        TypeSpec recordType = TypeFactory.createType(RECORD) ;
        token = nextToken();        //consume the RECORD

        // Push a symbol table for the RECORD type specification
        recordType.setAttribute(RECORD_SYMTAB,symTabStack.push());

        // Parse the field declarations.
        VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
        variableDeclarationsParser.setDefinition(FIELD);
        variableDeclarationsParser.parse(token);

        // Pop off the record's symbol table
        symTabStack.pop();

        // Synchronize at the END.
        token = synchronize(END_SET);

        // Look for the END.
        if(token.getType() == END){
            token = nextToken();        //consume END
        }else {
            errorHandler.flag(token, PascalErrorCode.MISSING_END,this);
        }

        return recordType;
    }
}
