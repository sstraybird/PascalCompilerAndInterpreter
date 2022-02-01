package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.backend.interpreter.RuntimeErrorCode;
import wci.frontend.pascal.parsers.AssignmentStatementParser;
import wci.frontend.pascal.parsers.CompundStatementParser;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.message.Message;
import wci.message.MessageType;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.LINE;

public class StatementExecutor extends Executor {
    /**
     * Constructor for subclasses.
     *
     * @param parent the parent executor
     */
    public StatementExecutor(Executor parent) {
        super(parent);
    }

    /**
     * Execute a statement
     * To be overridden by the specialized statement executor subclasses.
     * @param node the root node of the statement
     * @return null
     */
    public Object execute(ICodeNode node){
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl)node.getType();

        // Send a message about the current source line.
        sendSourceLineMessage(node);

        switch (nodeType){
            case COMPOUND:{
                CompoundExecutor compoundExecutor = new CompoundExecutor(this);
                return compoundExecutor.execute(node);
            }

            case ASSIGN:{
                AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);
                return assignmentExecutor.execute(node);
            }
            case LOOP:{
                LoopExecutor loopExecutor = new LoopExecutor(this);
                return loopExecutor.execute(node);
            }
            case IF:{
                IfExecutor ifExecutor = new IfExecutor(this);
                return ifExecutor.execute(node);
            }
            case SELECT:{
                SelectExecutor selectExecutor = new SelectExecutor(this);
                return selectExecutor.execute(node);
            }
            case NO_OP:return null;
            default:{
                errorHandler.flag(node, RuntimeErrorCode.UNIMPLEMENTED_FEATURE,this);
                return null;
            }
        }
    }

    /**
     * Send a message about the current source line.
     * @param node the statement node.
     */
    private void sendSourceLineMessage(ICodeNode node){
        Object lineNumber = node.getAttribute(LINE);

        // Send the SOURCE_LINE message
        if(lineNumber != null){
            sendMessage(new Message(MessageType.SOURCE_LINE,lineNumber));
        }
    }
}
