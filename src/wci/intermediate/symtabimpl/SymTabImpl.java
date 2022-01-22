package wci.intermediate.symtabimpl;

import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * <h1>SymTabImpl</h1>
 * <p>An implementation of the symbol table.</p>
 */
public class SymTabImpl extends TreeMap<String, SymTabEntry> implements SymTab {
    private int nestingLevel;

    public SymTabImpl(int nestingLevel){
        this.nestingLevel = nestingLevel;
    }

    @Override
    public int getNestingLevel() {
        return 0;
    }

    /**
     * Create and enter a new entry into the symbol table.
     * @param name the name of the entry.
     * @return the new entry.
     */
    @Override
    public SymTabEntry enter(String name) {
        SymTabEntry entry = SymTabFactory.createSymTabEntry(name,this);
        put(name,entry);
        return entry;
    }

    /**
     * Look up an existing symbol table entry.
     * @param name the name of the entry
     * @return the entry,or null if it does not exist.
     */
    @Override
    public SymTabEntry lookup(String name) {
        return get(name);
    }

    /**
     * @return a list of symbol table entries sorted by name.
     */
    @Override
    public ArrayList<SymTabEntry> sortedEntries() {
        Collection<SymTabEntry> entries = values();
        Iterator<SymTabEntry> iter = entries.iterator();
        ArrayList<SymTabEntry> list = new ArrayList<SymTabEntry>(size());
        while (iter.hasNext()){
            list.add(iter.next()) ;
        }
        return list;            // sorted list of entries.
    }
}
