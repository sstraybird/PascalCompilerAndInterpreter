package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;

/**
 *
 */
public class TypeDefinitionsParser extends DeclarationsParser{
    /**
     * Constructor
     *
     * @param parent the parent parser.
     */
    public TypeDefinitionsParser(PascalParserTD parent) {
        super(parent);
    }

    // Synchronization set for a type identifier.
    private static final EnumSet<PascalTokenType> IDENTIFIER_SET =
            DeclarationsParser.VAR_START_SET.clone();
    static {
        IDENTIFIER_SET.add(IDENTIFIER);
    }

    // Synchronization set for the = token.
    private static final EnumSet<PascalTokenType> EQUALS_SET =
            ConstantDefinitionsParser.CONSTANT_START_SET.clone();
    static {
        EQUALS_SET.add(EQUALS);
        EQUALS_SET.add(SEMICOLON);
    }

    // Synchronization set for what follows a definition or declaration.
    private static final EnumSet<PascalTokenType> FOLLOW_SET =
            EnumSet.of(SEMICOLON);

    // Synchronization set for the start of the next definition or declaration.
    private static final EnumSet<PascalTokenType> NEXT_START_SET =
            DeclarationsParser.VAR_START_SET.clone();
    static {
        NEXT_START_SET.add(SEMICOLON);
        NEXT_START_SET.add(IDENTIFIER);
    }
    /**
     * Parse type definitions
     * @param token the initial token.
     * @throws Exception if an error occurred.
     */
    @Override
    public SymTabEntry parse(Token token,SymTabEntry parentId) throws Exception {
        token = synchronize(IDENTIFIER_SET);

        // Loop to parse a sequence of type definitions separated by semicolons.
        while (token.getType() == IDENTIFIER){
            String name = token.getText().toLowerCase();
            SymTabEntry typeId = symTabStack.lookupLocal(name);

            // Enter the new identifier into the symbol table but don't set how it's defined yet.
            if(typeId == null){
                typeId = symTabStack.enterLocal(name);
                typeId.appendLineNumber(token.getLineNumber());
            }else {
                errorHandler.flag(token, PascalErrorCode.IDENTIFIER_REDEFINED,this);
                typeId = null;
            }

            token = nextToken();        //consume the identifier token

            //Synchronize on the = token
            token = synchronize(EQUALS_SET);
            if(token.getType() == EQUALS){
                token = nextToken();        // consume the =
            }else {
                errorHandler.flag(token,PascalErrorCode.MISSING_EQUALS,this);
            }

            // Parse the type specification.
            TypeSpecificationParser typeSpecificationParser = new TypeSpecificationParser(this);
            TypeSpec type = typeSpecificationParser.parse(token);

            // Set identifier to be a type and set its type specification.
            if(typeId !=null){
                typeId.setDefinition(DefinitionImpl.TYPE);
            }

            // Cross-link the type identifier and the type specification.
            if((type != null) && (typeId !=null)){
                if(type.getIdentifier() == null){
                    type.setIdentifier(typeId);
                }
                typeId.setTypeSpec(type);
            }else {
                token = synchronize(FOLLOW_SET);
            }

            token = currentToken();
            TokenType tokenType = token.getType();

            // Look for one or more semicolons after a definition.
            if(tokenType == SEMICOLON){
                while (token.getType() == SEMICOLON){
                    token = nextToken();        //consume the ;
                }
            }
            //If at the start of the next definition or declaration,then missing a semicolon.
            else if(NEXT_START_SET.contains(tokenType)){
                errorHandler.flag(token,PascalErrorCode.MISSING_SEMICOLON,this);
            }
            token = synchronize(IDENTIFIER_SET);
        }
        return null;
    }
}
