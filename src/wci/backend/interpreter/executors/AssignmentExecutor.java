package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.message.Message;
import wci.message.MessageType;

import java.util.ArrayList;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.DATA_VALUE;

/**
 * <h1>AssignmentExecutor</h1>
 * <p>Execute an assignment statement.</p>
 */
public class AssignmentExecutor extends StatementExecutor{
    /**
     * Constructor for subclasses.
     *
     * @param parent the parent executor
     */
    public AssignmentExecutor(Executor parent) {
        super(parent);
    }

    /**
     * Execute an assignment statement
     * @param node the root node of the statement
     * @return null
     */
    @Override
    public Object execute(ICodeNode node) {
        // The ASSIGN node's children are the target variable and the expression.
        ArrayList<ICodeNode> children = node.getChildren();
        ICodeNode variableNode = children.get(0);
        ICodeNode expressionNode = children.get(1);

        //Execute the expression and get its value.
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object value = expressionExecutor.execute(expressionNode);

        // Set the value as an attribute of the variable's symbol table entry.
        SymTabEntry variableId = (SymTabEntry)variableNode.getAttribute(ID);
        variableId.setAttribute(DATA_VALUE,value);

        sendMessage(node,variableId.getName(),value);
        ++executionCount;
        return null;
    }

    private void sendMessage(ICodeNode node,String variableName,Object value){
        Object lineNumber = node.getAttribute(LINE);

        //Send an ASSIGN message
        if(lineNumber!=null){
            sendMessage(new Message(MessageType.ASSIGN,new Object[]{lineNumber,variableName,value}));
        }
    }
}
