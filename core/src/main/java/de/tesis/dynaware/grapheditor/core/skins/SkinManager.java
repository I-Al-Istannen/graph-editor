/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorSkins;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultJointSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.core.view.ConnectionLayouter;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.util.Callback;

/**
 * Manages skins for all elements of a {@link GModel}.
 *
 * <p>
 * Provides lookup methods, for example to find the {@link GNodeSkin} instance
 * associated to a {@link GNode} instance.
 * </p>
 */
public class SkinManager implements SkinLookup, GraphEditorSkins {

    private final GraphEditor graphEditor;

    private Callback<GNode, GNodeSkin> nodeSkinFactory;
    private Callback<GConnector, GConnectorSkin> connectorSkinFactory;
    private Callback<GConnection, GConnectionSkin> connectionSkinFactory;
    private Callback<GJoint, GJointSkin> jointSkinFactory;
    private Callback<GConnector, GTailSkin> tailSkinFactory;

    private final Map<GNode, GNodeSkin> nodeSkins = new HashMap<>();
    private final Map<GConnector, GConnectorSkin> connectorSkins = new HashMap<>();
    private final Map<GConnection, GConnectionSkin> connectionSkins = new HashMap<>();
    private final Map<GJoint, GJointSkin> jointSkins = new HashMap<>();
    private final Map<GConnector, GTailSkin> tailSkins = new HashMap<>();

    private ConnectionLayouter connectionLayouter;
    private final Consumer<GSkin<?>> onPositionMoved = this::positionMoved;

    /**
     * Creates a new skin manager instance. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public SkinManager(final GraphEditor graphEditor) {
        this.graphEditor = graphEditor;
    }

    public void setConnectionLayouter(ConnectionLayouter connectionLayouter)
    {
        this.connectionLayouter = connectionLayouter;
    }

    @Override
    public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> skinFactory) {
        nodeSkinFactory = skinFactory;
    }

    @Override
    public void setConnectorSkinFactory(final Callback<GConnector, GConnectorSkin> connectorSkinFactory) {
        this.connectorSkinFactory = connectorSkinFactory;
    }

    @Override
    public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> connectionSkinFactory) {
        this.connectionSkinFactory = connectionSkinFactory;
    }

    @Override
    public void setJointSkinFactory(final Callback<GJoint, GJointSkin> jointSkinFactory) {
        this.jointSkinFactory = jointSkinFactory;
    }

    @Override
    public void setTailSkinFactory(final Callback<GConnector, GTailSkin> tailSkinFactory) {
        this.tailSkinFactory = tailSkinFactory;
    }

    /**
     * Adds a list of nodes.
     *
     * <p>
     * Skin instances will be created for these nodes and be available via the
     * lookup methods.
     * </p>
     *
     * @param nodesToAdd a list of {@link GNode} instances for which skin
     * instances should be created
     */
    public void addNodes(final List<GNode> nodesToAdd) {
        if (nodesToAdd != null && !nodesToAdd.isEmpty()) {
            // prevent ConcurrentModification
            final GNode[] updates = nodesToAdd.toArray(new GNode[nodesToAdd.size()]);
            for (final GNode node : updates) {
                nodeSkins.computeIfAbsent(node, this::createNodeSkin);
                addConnectors(node);
            }
        }
    }

    /**
     * Removes a list of nodes.
     *
     * <p>
     * The skin instances for these nodes will be removed (if they exist).
     * </p>
     *
     * @param nodesToRemove a list of {@link GNode} instances for which skin
     * instances should be removed
     */
    public void removeNodes(final List<GNode> nodesToRemove) {
        if (nodesToRemove != null && !nodesToRemove.isEmpty()) {
            // prevent ConcurrentModification
            final GNode[] updates = nodesToRemove.toArray(new GNode[nodesToRemove.size()]);
            for (final GNode node : updates) {
                final GNodeSkin removedSkin = nodeSkins.remove(node);
                if (removedSkin != null) {
                    removedSkin.dispose();
                }
                removeConnectors(node.getConnectors());
            }
        }
    }

    /**
     * Updates a list of nodes
     *
     * <p>
     * The connector skins for these nodes will be created and re-set.
     * </p>
     *
     * @param nodesToUpdate a list of {@link GNode} instances which should be
     * updated
     */
    public void updateNodes(final List<GNode> nodesToUpdate) {
        if (nodesToUpdate != null && !nodesToUpdate.isEmpty()) {
            // prevent ConcurrentModification
            final GNode[] updates = nodesToUpdate.toArray(new GNode[nodesToUpdate.size()]);
            for (final GNode node : updates) {
                removeConnectors(node.getConnectors());
                addConnectors(node);
            }
        }
    }

    /**
     * Removes a list of connectors.
     *
     * <p>
     * The skin instances for these connectors will be removed (if they exist).
     * </p>
     *
     * @param connectorsToRemove a list of {@link GConnector} instances for
     * which skin instances should be removed
     */
    public void removeConnectors(final List<GConnector> connectorsToRemove) {
        if (connectorsToRemove != null && !connectorsToRemove.isEmpty()) {
            // prevent ConcurrentModification
            final GConnector[] updates = connectorsToRemove.toArray(new GConnector[connectorsToRemove.size()]);
            for (final GConnector connector : updates) {
                final GConnectorSkin removedSkin = connectorSkins.remove(connector);
                if (removedSkin != null) {
                    removedSkin.dispose();
                }
                final GTailSkin removedTailSkin = tailSkins.remove(connector);
                if(removedTailSkin != null) {
                    removedTailSkin.dispose();
                }
            }
        }
    }

    /**
     * Adds a list of connections.
     *
     * <p>
     * Skin instances will be created for these connections and be available via
     * the lookup methods.
     * </p>
     *
     * @param connectionsToAdd a list of {@link GConnection} instances for which
     * skin instances should be created
     */
    public void addConnections(final List<GConnection> connectionsToAdd) {
        if (connectionsToAdd != null && !connectionsToAdd.isEmpty()) {
            // prevent ConcurrentModification
            final GConnection[] updates = connectionsToAdd.toArray(new GConnection[connectionsToAdd.size()]);
            for (final GConnection connection : updates) {
                final GConnectionSkin connectionSkin = connectionSkins.computeIfAbsent(connection, this::createConnectionSkin);
                final List<GJointSkin> connectionJointSkins = connection.getJoints().stream()
                        .map(joint -> jointSkins.computeIfAbsent(joint, this::createJointSkin)).collect(Collectors.toList());
                connectionSkin.setJointSkins(connectionJointSkins);
            }
        }
    }

    /**
     * Removes a list of connections.
     *
     * <p>
     * The skin instances for these connections will be removed (if they exist).
     * </p>
     *
     * @param connectionsToRemove a list of {@link GConnection} instances for
     * which skin instances should be removed
     */
    public void removeConnections(final List<GConnection> connectionsToRemove) {
        if (connectionsToRemove != null && !connectionsToRemove.isEmpty()) {
            // prevent ConcurrentModification
            final GConnection[] updates = connectionsToRemove.toArray(new GConnection[connectionsToRemove.size()]);
            for (final GConnection connection : updates) {
                final GConnectionSkin removedSkin = connectionSkins.remove(connection);
                if (removedSkin != null) {
                    removedSkin.dispose();
                }
            }
        }
    }

    /**
     * Adds the given list of joints to a particular connection.
     *
     * <p>
     * Skin instances will be created for these joints and be available via the
     * lookup methods. Furthermore connection's list of joints will be updated.
     * </p>
     *
     * @param connection the {@link GConnection} to which joints should be added
     * @param jointsToAdd a list of {@link GJoint} instances for which skin
     * instances should be created and added
     */
    public void addJoints(final GConnection connection, final List<GJoint> jointsToAdd) {
        if (jointsToAdd != null && !jointsToAdd.isEmpty()) {
            jointsToAdd.forEach(joint -> jointSkins.computeIfAbsent(joint, this::createJointSkin));
        }
        if (connection != null) {
            final List<GJointSkin> connectionJointSkins = connection.getJoints().stream().map(this::lookupJoint).collect(Collectors.toList());
            lookupConnection(connection).setJointSkins(connectionJointSkins);
        }
    }

    /**
     * Removes a list of joints.
     *
     * <p>
     * The skin instances for these joints will be removed (if they exist).
     * </p>
     *
     * @param jointsToRemove a list of {@link GJoint} instances for which skin
     * instances should be removed
     */
    public void removeJoints(final List<GJoint> jointsToRemove) {
        if (jointsToRemove != null && !jointsToRemove.isEmpty()) {
            // prevent ConcurrentModification
            final GJoint[] updates = jointsToRemove.toArray(new GJoint[jointsToRemove.size()]);
            for (final GJoint joint : updates) {
                final GJointSkin removedSkin = jointSkins.remove(joint);
                if (removedSkin != null) {
                    removedSkin.dispose();
                }
            }
        }
    }

    /**
     * Initializes all node and joint skins, so that their layout values are
     * reloaded from their model instances.
     */
    public void initializeAll() {
        nodeSkins.values().forEach(GNodeSkin::initialize);
        jointSkins.values().forEach(GJointSkin::initialize);
    }

    @Override
    public GNodeSkin lookupNode(final GNode node) {
        return nodeSkins.get(node);
    }

    @Override
    public GConnectorSkin lookupConnector(final GConnector connector) {
        return connectorSkins.get(connector);
    }

    @Override
    public GConnectionSkin lookupConnection(final GConnection connection) {
        return connectionSkins.get(connection);
    }

    @Override
    public GJointSkin lookupJoint(final GJoint joint) {
        return jointSkins.get(joint);
    }

    @Override
    public GTailSkin lookupTail(final GConnector connector) {
        return tailSkins.get(connector);
    }

    /**
     * Adds a list of connector skins for the given node.
     *
     * <p>
     * The node skin's list of connector skins will be updated.
     * </p>
     *
     * @param node the {@link GNode} whose connectors should be added
     */
    private void addConnectors(final GNode node) {
        final List<GConnectorSkin> nodeConnectorSkins = new ArrayList<>();

        for (final GConnector connector : node.getConnectors()) {
            nodeConnectorSkins.add(connectorSkins.computeIfAbsent(connector, this::createConnectorSkin));
            tailSkins.computeIfAbsent(connector, this::createTailSkin);
        }

        nodeSkins.get(node).setConnectorSkins(nodeConnectorSkins);
    }

    private GConnectorSkin createConnectorSkin(final GConnector connector) {
        GConnectorSkin skin = connectorSkinFactory == null ? null : connectorSkinFactory.call(connector);
        if (skin == null) {
            skin = new DefaultConnectorSkin(connector);
        }
        skin.setGraphEditor(graphEditor);
        return skin;
    }

    private GTailSkin createTailSkin(final GConnector connector) {
        GTailSkin skin = tailSkinFactory == null ? null : tailSkinFactory.call(connector);
        if (skin == null) {
            skin = new DefaultTailSkin(connector);
        }
        skin.setGraphEditor(graphEditor);
        return skin;
    }

    private GConnectionSkin createConnectionSkin(final GConnection connection) {
        GConnectionSkin skin = connectionSkinFactory == null ? null : connectionSkinFactory.call(connection);
        if (skin == null) {
            skin = new DefaultConnectionSkin(connection);
        }
        skin.setGraphEditor(graphEditor);
        return skin;
    }

    private GJointSkin createJointSkin(final GJoint joint) {
        GJointSkin skin = jointSkinFactory == null ? null : jointSkinFactory.call(joint);
        if (skin == null) {
            skin = new DefaultJointSkin(joint);
        }
        skin.setGraphEditor(graphEditor);
        skin.getRoot().setEditorProperties(graphEditor.getProperties());
        skin.impl_setOnPositionMoved(onPositionMoved);
        return skin;
    }

    private GNodeSkin createNodeSkin(final GNode node) {
        GNodeSkin skin = nodeSkinFactory == null ? null : nodeSkinFactory.call(node);
        if (skin == null) {
            skin = new DefaultNodeSkin(node);
        }
        skin.setGraphEditor(graphEditor);
        skin.getRoot().setEditorProperties(graphEditor.getProperties());
        skin.impl_setOnPositionMoved(onPositionMoved);
        skin.initialize();
        return skin;
    }

    private void positionMoved(final GSkin<?> pMovedSkin)
    {
        final ConnectionLayouter layouter = connectionLayouter;
        if (layouter == null)
        {
            return;
        }
        if (pMovedSkin instanceof GNodeSkin)
        {
            // redraw all connections attached to each connector of the GNode:
            for (final GConnector connector : ((GNodeSkin) pMovedSkin).getItem().getConnectors())
            {
                layouter.redraw(connector.getConnections());
            }
        }
        else if (pMovedSkin instanceof GJointSkin)
        {
            // redraw the GConnection of the GJoint:
            layouter.redraw(((GJointSkin) pMovedSkin).getItem().getConnection());
        }
    }
}
