package de.tub.pes.syscir.dependencies;

import de.tub.pes.syscir.dependencies.SdgNode.SdgNodeId;

public class SdgEdge extends DgEdge<SdgEdge, SdgNode, SdgNodeId> {

    private boolean inserted;

    public SdgEdge(EdgeType type, SdgNode source, SdgNode target, boolean insert) {
        super(type, source, target);

        if (insert && !insert()) {
            throw new IllegalStateException("edge already present");
        }
    }

    /**
     * Inserts this edge as incoming/outcoing for the target/source node respectively.
     * 
     * This is done atomically, so the edge is either inserted at both nodes or at none of them.
     * 
     * @return true if the insertion was successfull, false if an equal edge was aready present in one
     *         of the nodes.
     * @throws IllegalStateException if this edge instance has already been inserted before
     */
    public boolean insert() throws IllegalStateException {
        if (this.inserted) {
            throw new IllegalStateException("already inserted");
        }
        this.inserted = true;

        if (!getSource().addOutgoing(this)) {
            return false;
        }
        try {
            if (!getTarget().addIncoming(this)) {
                getSource().removeOutgoing(this);
                return false;
            }
        } catch (Exception e) {
            getSource().removeOutgoing(this);
            throw e;
        }
        return true;
    }

}
