package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.List;

/**
 * <h1>LoopExecutor</h1>
 * <p>Execute a loop statement</p>
 */
public class LoopExecutor extends StatementExecutor{
    /**
     * Constructor for subclasses.
     *
     * @param parent the parent executor
     */
    public LoopExecutor(Executor parent) {
        super(parent);
    }

    /**
     * Execute a loop statement
     * @param node the root node of the statement
     * @return null
     */
    @Override
    public Object execute(ICodeNode node) {
        boolean exitLoop = false;
        ICodeNode exprNode = null;
        List<ICodeNode> loopChildren = node.getChildren();

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        while (!exitLoop){
            ++executionCount;           //count the loop statement itself
            //Execute the children of the LOOP node.
            for(ICodeNode child:loopChildren){
                ICodeNodeTypeImpl childType = (ICodeNodeTypeImpl)child.getType();

                // TEST node?
                if(childType == ICodeNodeTypeImpl.TEST){
                    if(exprNode == null){
                        exprNode = child.getChildren().get(0);
                    }
                    exitLoop = (Boolean)expressionExecutor.execute(exprNode);
                }
                //Statement node.
                else {
                    statementExecutor.execute(child);
                }

                //Exit if the TEST expression value is true.
                if(exitLoop){
                    break;
                }
            }
        }
        return null;
    }
}
