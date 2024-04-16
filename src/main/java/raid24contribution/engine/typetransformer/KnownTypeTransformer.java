package raid24contribution.engine.typetransformer;

import java.util.List;
import raid24contribution.engine.Environment;
import raid24contribution.engine.TransformerFactory;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.variables.SCKnownType;

/**
 * This is the base interface for every known type transformer in STATE. Known types are those
 * complex base types defined in the SystemC library. As the KaSCPar SystemC parser does not process
 * C++ include directives, the definition of these types is not available to STATE and can not be
 * parsed automatically. Also, the original SystemC classes may not meet the requirements of the
 * STATE transformation rules.
 * <p>
 * For these types, STATE offers a mechanism to introduce them aside from the model that is
 * currently to be analyzed. The code to represent these types is encapsulated in a known type
 * transformer, which is a concrete class which implements this interface either directly or through
 * extending {@link AbstractTypeTransformer}.
 * <p>
 * If such a type is used, STATE creates the type transformer using the {@link TransformerFactory}.
 * The types definition file contains the information which type transformer class to load for which
 * method.
 * <p>
 * In addition to the methods which are required to fully represent a complex type, a known type
 * transformer contains the methods to manipulate and save the name of an optional extra file of any
 * kind that contains the implementation of the type, or any other information that is not java
 * code. The transformer factory obtains the name of the implementation file from the types
 * definition file. Please see the documentation on configuration files.
 * <p>
 * It is recommended to implement this interface by extending the class
 * {@link AbstractTypeTransformer}. That type contains standard implementations of all the interface
 * methods, which will be sufficient in most cases. Please see the existing implementations for
 * examples of known type transformers.
 * 
 * 
 */
public interface KnownTypeTransformer {
    
    /**
     * Sets the name of a file that contains implementation information of the type. Use is optional.
     * 
     * @param file
     */
    public void setImplementation(String file);
    
    /**
     * Returns the implementation file name, if one is used for the type. Returns null if no file is
     * used.
     * 
     * @return
     */
    public String getImplementation();
    
    /**
     * This method is intended to introduce a new SystemC type to STATE. Optimally it creates a single
     * {@link Module} that represents the type. There are no restrictions on how that is done. The file
     * whose is set through the {@link #setImplementation(String)} method may be used.
     * <p>
     * This method is called each time a variable declaration with the associated type occurs. The
     * implementation of this method is responsible for preventing the creation of doubles.
     * 
     * @param name - the name of the type
     * @param e the environment
     */
    public void createType(Environment e);
    
    /**
     * Implementations of this method create a global instance (in sc_main method) of the type
     * associated to this type transformer. Generally this would only mean to create a
     * {@link ModuleInstance} of the {@link Module} that represents this type and save it to the SC
     * model, along with the given constructor arguments. However, some types might require additional
     * actions and for those cases this method is part of the interface. {@link AbstractTypeTransformer}
     * contains a standard implementation.
     * 
     * @param instName Name of the variable
     * @param params Constructor parameter list
     * @param templateParams Template arguments to the type
     * @param environment Environment map that hold transient information through the whole parsing
     *        process
     * @param ta TA model
     * @param sc SC model
     */
    public SCKnownType createInstance(String instName, Environment e, boolean stat, boolean cons,
            List<String> other_mods);
    
    public SCKnownType initiateInstance(String v, List<Expression> para, Environment e, boolean stat, boolean cons,
            List<String> other_mods);
    
    // public void initiateInstance(SCKnownType inst, List<Expression> params,
    // Environment e);
}