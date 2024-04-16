package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.ForLoopExpression;

/**
 * we search for the 3 Expressions which control the for-loop the for_init, the for_condition and
 * the for_update we save them, and handle the loop-body afterwards we creat a new forLoopExpression
 * and add it to the stack
 *
 * The user may annotate the maximum iteration frequency for timing analysis. The annotation has to
 * be the first comment inside the loop containing the fixed string "// MAX_ITERATIONS = X " (case
 * and space sensitive), where X is an positive integer value. If there is a loop annotation inside
 * the block we store the value.
 * 
 */
public class ForStatementTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        handleNode(findChildNode(node, "for_init"), e);
        
        Expression init = e.getExpressionStack().pop();
        // TODO handle for ( ;...)
        
        handleNode(findChildNode(node, "for_condition"), e);
        Expression cond = e.getExpressionStack().pop();
        
        handleNode(findChildNode(node, "for_update"), e);
        Expression update = e.getExpressionStack().pop();
        
        List<Expression> for_body = new ArrayList<>();
        
        Node for_block = findChildNode(findChildNode(node, "for_block"), "block");
        List<Node> nodes = findChildNodes(for_block, "block_statement");
        
        int maxIterations = -1; // -1 means unknown
        // handle simple infinite loop ... infinite for-loop -> who does this???
        Node comment = findChildNode(for_block, "comment");
        if (comment != null) {
            Node nameNode = comment.getAttributes().getNamedItem("name");
            String name = nameNode.getNodeValue();
            if (name.startsWith("// MAX_ITERATIONS = ")) {
                maxIterations = Integer.valueOf(name.substring("// MAX_ITERATIONS = ".length()));
            }
        }
        
        int size;
        for (Node n : nodes) {
            size = e.getExpressionStack().size();
            handleNode(n, e);
            if (e.getExpressionStack().size() == size) {
                // non handled Statement, like Output-Statement
            } else {
                for (int i = size; i < e.getExpressionStack().size(); i++) {
                    for_body.add(e.getExpressionStack().get(i));
                }
                while (e.getExpressionStack().size() > size) {
                    e.getExpressionStack().pop();
                }
            }
        }
        
        ForLoopExpression fle = new ForLoopExpression(node, "", init, cond, update, for_body, maxIterations);
        e.getExpressionStack().add(fle);
    }
}