package de.tub.pes.syscir.dependencies;

public interface CfgEdge<NodeType extends CfgNode<NodeType, EdgeType, ?, ?>, EdgeType extends CfgEdge<NodeType, EdgeType>> {

    NodeType getSource();

    NodeType getTarget();

}
