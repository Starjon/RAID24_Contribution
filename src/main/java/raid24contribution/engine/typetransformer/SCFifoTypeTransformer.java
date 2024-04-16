package raid24contribution.engine.typetransformer;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.engine.Environment;
import raid24contribution.engine.TransformerFactory;
import raid24contribution.engine.util.Constants;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCVariableDeclarationExpression;
import raid24contribution.sc_model.variables.SCKnownType;

/**
 * This class represents a fifo_type channel.
 * 
 * @author Pfeffer
 */
public class SCFifoTypeTransformer extends AbstractTypeTransformer {
    
    private static Logger logger = LogManager.getLogger(SCFifoTypeTransformer.class.getName());
    
    /**
     * creates the SCClasses for sc_fifo dependending on data type & fifo size classes will be named:
     * sc_fifo_TYPE / sc_fifo_TYPE_SIZE therefore the method reads the sc_fifo*.ast.xml implementations
     * and replaces GTYPE and GSIZE
     */
    @Override
    public void createType(Environment e) {
        // sc_fifo declaration without instantiation
        // build generic template without fifo size: sc_fifo_TYPE
        if (!e.getLastType().isEmpty() && !e.getLastType().peek().equals("sc_fifo")) {
            
            String fifoType = e.getLastType().peek();
            this.name = createName(fifoType);
            if (!existsType(this.name, e)) {
                Environment temp = createEnvironment(e, fifoType);
                temp.getCurrentClass().setName(this.name);
                e.integrate(temp);
            }
        }
        // fifo instantiation
        // build generic type with size: sc_fifo_TYPE_SIZE
        // default size = 16
        else if (!e.getLastType().isEmpty() && e.getLastType().peek().equals("sc_fifo")
                && !e.getLastArgumentList().isEmpty()) {
            String size = ((ConstantExpression) e.getLastArgumentList().get(0)).getValue();
            
            
            String type = e.getLastType_TemplateArguments().get(0);
            // Build template and replace BUF_SIZE
            this.name = createName(type, size);
            if (!existsType(this.name, e)) {
                Environment temp = createEnvironment(e, type);
                temp.getCurrentClass().setName(this.name);
                addSize(size, temp.getCurrentClass());
                e.integrate(temp);
            }
        }
        // Size not known, type should already be created
        else {
            int x = 0;
        }
    }
    
    /**
     * returns a KnownType for the instantiated sc_fifo_*
     */
    @Override
    public SCKnownType initiateInstance(String instName, List<Expression> params, Environment e, boolean stat,
            boolean cons, List<String> other_mods) {
        
        // create (now) known type
        SCKnownType kt = super.initiateInstance(instName, new LinkedList<>(), e, stat, cons, other_mods);
        
        return kt;
    }
    
    /**
     * creates an Environment-Object of the sc_fifo implementation therefore it uses -
     * sc_fifo_complex.ast.xml for data type with generic parameters (i.e. sc_uint<XX>) -
     * sc_fifo.ast.xml for simple data typen (i.e. bool, int, ...)
     * 
     * @param e
     * @param type
     * @return
     */
    private Environment createEnvironment(Environment e, String type) {
        
        Pair<String, String>[] replacements;
        
        if (type.contains("<")) {
            replacements = new Pair[3];
            this.impl = TransformerFactory.getImplementation("sc_fifo", "sc_fifo_generic.ast.xml");
            String typeIdentifier = type.substring(0, type.indexOf("<"));
            String length = type.substring(type.indexOf("<") + 1, type.length() - 1);
            replacements[0] = new Pair<>(Constants.GENERIC_TYPE, typeIdentifier);
            replacements[1] = new Pair<>(Constants.GENERIC_TYPE_LENGTH, length);
            replacements[2] = new Pair<>("sc_fifox", "sc_fifo_" + typeIdentifier);
        } else {
            replacements = new Pair[2];
            replacements[0] = new Pair<>(Constants.GENERIC_TYPE, type);
            replacements[1] = new Pair<>("sc_fifox", "sc_fifo_" + type);
        }
        
        return super.createGenericType(e, replacements);
    }
    
    /**
     * eliminates special characters from the data type so that valid names for the corresponding
     * SCClass are created
     *
     * @param type
     * @return
     */
    private static String typeForTemplate(String type) {
        return type.replace(" ", "").replace("<", "").replace(">", "").replace("_", "");
    }
    
    /**
     * Changes the size of the sc_fifo to the given one DOES NOT CHANGE THE NAME!!!
     * 
     * @param size
     * @param scClass
     */
    public static void addSize(String size, SCClass scClass) {
        SCVariable bufferSizeVar = scClass.getMemberByName("BUF_SIZE");
        if (bufferSizeVar != null) {
            SCVariableDeclarationExpression declarationExpression = bufferSizeVar.getDeclaration();
            ConstantExpression initialValue = (ConstantExpression) declarationExpression.getInitialValues().get(0);
            initialValue.setValue(size + "");
        } else {
            logger.warn(
                    "It seems that sc_fifo implementation was changed but SCFifoTypeTransformer was not updated! Buffer size could not be changed");
        }
    }
    
    /**
     * creates the name of the class: sc_fifo_TYPE
     *
     * @param type
     * @return
     */
    public static String createName(String type) {
        return "sc_fifo" + Constants.GENERIC_TYPE_DELIMITER + typeForTemplate(type);
    }
    
    /**
     * creates the name of the class: sc_fifo_TYPE_SIZE
     *
     * @param type
     * @param size
     * @return
     */
    public static String createName(String type, String size) {
        return "sc_fifo" + Constants.GENERIC_TYPE_DELIMITER + typeForTemplate(type) + Constants.GENERIC_TYPE_DELIMITER
                + size;
    }
    
    /**
     * appends the size to the given fifoName
     * 
     * @param fifoName should include the type
     * @param size
     * @return fifoName_SIZE
     */
    public static String appendSize(String fifoName, String size) {
        return fifoName + Constants.GENERIC_TYPE_DELIMITER + size;
    }
    
}