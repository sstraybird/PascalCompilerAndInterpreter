package wci.frontend.pascal;

import wci.frontend.Source;
import wci.frontend.Token;

public class PascalToken extends Token {
    /**
     * Constructor
     *
     * @param source the source from where to fetch the token's character
     * @throws Exception if an error occurred.
     */
    public PascalToken(Source source) throws Exception {
        super(source);
    }
}
