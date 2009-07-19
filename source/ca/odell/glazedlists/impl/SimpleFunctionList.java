package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.event.ListEvent;

/**
 * A {@link TransformedList} that maps each element of a source list to a target element by use
 * of a specified {@link Function}.
 * <p>
 * <strong><font color="#FF0000">Warning:</font></strong> This class is thread ready but not
 * thread safe. See {@link EventList} for an example of thread safe code.
 *
 * @see TransformedList
 * @author Holger Brands
 */
public final class SimpleFunctionList<S, E> extends TransformedList<S, E> {

    /** function to transform each list element. */
    private final Function<S,E> function;

    /**
     * Constructs a SimpleFunctionList that maps the elements of the given source list by the
     * specified function.
     *
     * @param source the source list to transform
     * @param function the mapping function
     */
    public SimpleFunctionList(EventList<S> source, Function<S, E> function) {
        super(source);
        Preconditions.checkNotNull(function, "mapping function is undefined");
        this.function = function;
        source.addListEventListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int index) {
        final S elem = source.get(index);
        return function.evaluate(elem);
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<S> listChanges) {
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return false;
    }
}
