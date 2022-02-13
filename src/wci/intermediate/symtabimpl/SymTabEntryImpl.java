package wci.intermediate.symtabimpl;

import wci.intermediate.TypeSpec;
import wci.intermediate.Definition;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabKey;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <h1>SymTabEntryImpl</h1>
 *
 * <p>An implementation of a symbol table entry.</p>
 */
public class SymTabEntryImpl extends HashMap<SymTabKey,Object> implements SymTabEntry {

    private String name;            //entry name
    private SymTab symTab;          // parent symbol table
    private ArrayList<Integer> lineNumbers;     //source line numbers

    private Definition definition;      //how the identifier is defined
    private TypeSpec typeSpec;          // type specification

    /**
     * Constructor
     * @param name the name of the entry.
     * @param symTab the symbol table that contains this entry.
     */
    public SymTabEntryImpl(String name,SymTab symTab){
        this.name = name;
        this.symTab = symTab;
        this.lineNumbers = new ArrayList<Integer>();
    }

    /**
     * Getter
     * @return the name of the entry.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Getter
     * @return the symbol table that contains this entry.
     */
    @Override
    public SymTab getSymTab() {
        return symTab;
    }

    /**
     * Append a source line number to the entry
     * @param lineNumber the line number to append.
     */
    @Override
    public void appendLineNumber(int lineNumber) {
        lineNumbers.add(lineNumber);
    }

    @Override
    public ArrayList<Integer> getLineNumbers() {
        return lineNumbers;
    }

    /**
     * Set an attribute of the entry.
     * @param key the attribute key.
     * @param value the attribute value.
     */
    @Override
    public void setAttribute(SymTabKey key, Object value) {
        put(key,value);
    }

    /**
     * Get the value of an attribute of the entry.
     * @param key the attribute key.
     * @return the attribute value.
     */
    @Override
    public Object getAttribute(SymTabKey key) {
        return get(key);
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    @Override
    public void setTypeSpec(TypeSpec typeSpec) {
        this.typeSpec = typeSpec;
    }

    @Override
    public TypeSpec getTypeSpec() {
        return typeSpec;
    }
}
