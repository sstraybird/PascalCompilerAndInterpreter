package wci.frontend.pascal.tokens;

import com.sun.org.apache.xpath.internal.operations.String;
import wci.frontend.Source;
import wci.frontend.pascal.PascalToken;




import static wci.frontend.Source.EOF;
import static wci.frontend.pascal.PascalErrorCode.UNEXPECTED_EOF;
import static wci.frontend.pascal.PascalTokenType.ERROR;
import static wci.frontend.pascal.PascalTokenType.STRING;

public class PascalStringToken extends PascalToken {
    /**
     * Constructor
     *
     * @param source the source from where to fetch the token's character
     * @throws Exception if an error occurred.
     */
    public PascalStringToken(Source source) throws Exception {
        super(source);
    }

    /**
     * Extract a Pascal string token from the source
     * @throws Exception if an error occurred.
     */
    @Override
    protected void extract() throws Exception {
        StringBuilder textBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        char currentChar = nextChar();          //consume initial quote
        textBuffer.append('\'') ;

        // Get string characters.
        do{
            // Replace any whitespace character with a blank
            if(Character.isWhitespace(currentChar)){
                currentChar = ' ';
            }
            if((currentChar != '\'') && (currentChar != EOF)){
                textBuffer.append(currentChar);
                valueBuffer.append(currentChar);
                currentChar = nextChar();
            }
            //Quote? Each pair of adjacent quotes represents a single-quote
            if(currentChar == '\''){
                while ((currentChar == '\'') && (peekChar() == '\'')){
                    textBuffer.append("''");
                    valueBuffer.append(currentChar);        //append single-quote
                    currentChar = nextChar();               //consume pair of quotes
                    currentChar = nextChar();
                }
            }
        }while (((currentChar !='\'') && (currentChar!=EOF)));

        if(currentChar == '\''){
            nextChar();     //consume final quote
            textBuffer.append('\'');

            type= STRING;
            value = valueBuffer.toString();
        }else {
            type = ERROR;
            value = UNEXPECTED_EOF;
        }
        text = textBuffer.toString();
    }
}
