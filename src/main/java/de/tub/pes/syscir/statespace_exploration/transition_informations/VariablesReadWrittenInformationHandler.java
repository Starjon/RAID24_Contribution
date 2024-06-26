package de.tub.pes.syscir.statespace_exploration.transition_informations;

import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.statespace_exploration.LocalState;
import de.tub.pes.syscir.statespace_exploration.ProcessState;
import de.tub.pes.syscir.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler;
import de.tub.pes.syscir.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler.AccessedVariablesInformation;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.ExpressionCrawler;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.ExpressionCrawler.ExecutionConditions;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;


public class VariablesReadWrittenInformationHandler<ValueT extends AbstractedValue<ValueT, BoolT, ?>, BoolT extends AbstractedValue<BoolT, BoolT, Boolean>>
implements InformationHandler<VariablesReadWrittenInformation<BoolT>, ValueT> {

    private final Predicate<Variable<?, ?>> variableConsiderationPredicate;
    private final Function<Boolean, BoolT> determinedBoolGetter;

    public VariablesReadWrittenInformationHandler(Predicate<Variable<?, ?>> variableConsiderationPredicate,
            Function<Boolean, BoolT> determinedBoolGetter) {
        this.variableConsiderationPredicate = Objects.requireNonNull(variableConsiderationPredicate);
        this.determinedBoolGetter = Objects.requireNonNull(determinedBoolGetter);
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> getNeutralInformation() {
        return new VariablesReadWrittenInformation<>();
    }

    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> VariablesReadWrittenInformation<BoolT> handleStartOfCode(
            TransitionResult<?, ?, ?, VariablesReadWrittenInformation<BoolT>, ?> currentState, LocalStateT localState) {
        return currentState.transitionInformation();
    }

    // TODO: make better (unnecessarily convoluted at the moment, seperate methods in information for
    // adding read/written)
    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> VariablesReadWrittenInformation<BoolT> handleExpressionEvaluation(
            Expression evaluated, int comingFrom,
            TransitionResult<?, ?, ?, VariablesReadWrittenInformation<BoolT>, ?> resultingState,
            LocalStateT localState) {
        // TODO: the current condition contains values that only hold at the moment the control decision
        // was made. the may have been changed afterwards without retroactively effecting the control
        // decision, making the current value obsolete. how to deal with that?

        if (evaluated instanceof SCVariableExpression || evaluated instanceof SCPortSCSocketExpression) {
            AccessedVariablesInformation readVariables =
                    localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_READ_KEY);
            if (readVariables == null) {
                return resultingState.transitionInformation();
            }
            Map<Variable<?, ?>, BoolT> informationMap =
                    createInformationMap(readVariables, getCurrentCondition(localState));
            return resultingState.transitionInformation()
                    .concat(new VariablesReadWrittenInformation<>(informationMap, Map.of()));
        } else if (evaluated instanceof BinaryExpression be) {
            if (be.getOp().equals("=")) {
                AccessedVariablesInformation writtenVariables =
                        localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_WRITTEN_KEY);
                if (writtenVariables == null) {
                    return resultingState.transitionInformation();
                }
                Map<Variable<?, ?>, BoolT> informationMap =
                        createInformationMap(writtenVariables, getCurrentCondition(localState));
                return resultingState.transitionInformation()
                        .concat(new VariablesReadWrittenInformation<>(Map.of(), informationMap));
            }
        }

        return resultingState.transitionInformation();
    }

    protected <LocalStateT extends LocalState<LocalStateT, ValueT>> BoolT getCurrentCondition(LocalStateT localState) {
        ExecutionConditions<BoolT> currentExecutionConditions =
                localState.getStateInformation(ExpressionCrawler.executionConditionsKey());
        if (currentExecutionConditions == null) {
            return this.determinedBoolGetter.apply(true);
        }

        return currentExecutionConditions.getConditions().stream().reduce(this.determinedBoolGetter.apply(true),
                (b1, b2) -> b1.getAbstractedLogic().and(b1, b2));
    }

    protected Map<Variable<?, ?>, BoolT> createInformationMap(AccessedVariablesInformation accessedVariables,
            BoolT condition) {
        Map<Variable<?, ?>, BoolT> result = new LinkedHashMap<>();
        for (Variable<?, ?> var : accessedVariables) {
            if (this.variableConsiderationPredicate.test(var))
                result.put(var, condition);
        }
        return result;
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> handleProcessWaitedForDelta(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, VariablesReadWrittenInformation<BoolT> currentInformation) {
        return currentInformation;
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> handleProcessWaitedForTime(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, VariablesReadWrittenInformation<BoolT> currentInformation) {
        return currentInformation;
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> handleProcessWaitedForEvents(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, Set<Event> events, EventBlocker blockerBefore,
            VariablesReadWrittenInformation<BoolT> currentInformation) {
        return currentInformation;
    }

}
