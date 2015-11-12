/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import javafx.scene.CacheHint;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.scene.layout.Pane;

/**
 * The {@link Region} that all visual elements in the graph editor are added to.
 *
 * <p>
 * There is one instance of this class per {@link DefaultGraphEditor}. It is the
 * outermost JavaFX node of the editor.
 * </p>
 *
 * <p>
 * The view currently has two layers - a <b>node</b> layer and a
 * <b>connection</b> layer. The node layer is in front. Graph nodes are added to
 * the node layer, while connections, joints, and tails are added to the
 * connection layer. This means nodes will always be in front of connections.
 * </p>
 *
 * <p>
 * Calling toFront() or toBack() on the associated JavaFX nodes will just
 * reposition them inside their layer. The layers always have the same
 * dimensions as the editor region itself.
 * </p>
 */
public class GraphEditorView extends Region {

    private static final String STYLESHEET_VIEW = "view.css";
    private static final String STYLESHEET_DEFAULTS = "defaults.css";

    private static final String STYLE_CLASS = "graph-editor";
    private static final String STYLE_CLASS_NODE_LAYER = "graph-editor-node-layer";
    private static final String STYLE_CLASS_CONNECTION_LAYER = "graph-editor-connection-layer";

    private static final String NODE_LAYER_ID = "nodeLayer";
    private static final String CONNECTION_LAYER_ID = "connectionLayer";

    private final Pane nodeLayer = new Pane();
    private final Pane connectionLayer = new Pane();

    private final GraphEditorGrid grid = new GraphEditorGrid();
    private ConnectionLayouter connectionLayouter;

    private final SelectionBox selectionBox = new SelectionBox();

    private GraphEditorProperties editorProperties;

    /**
     * Creates a new {@link GraphEditorView} to which skin instances can be
     * added and removed.
     */
    public GraphEditorView() {

        getStylesheets().add(GraphEditorView.class.getResource(STYLESHEET_VIEW).toExternalForm());
        getStylesheets().add(GraphEditorView.class.getResource(STYLESHEET_DEFAULTS).toExternalForm());

        getStyleClass().addAll(STYLE_CLASS);

        setMaxWidth(GraphEditorProperties.DEFAULT_MAX_WIDTH);
        setMaxHeight(GraphEditorProperties.DEFAULT_MAX_HEIGHT);

        initializeLayers();
    }

    /**
     * Sets the connection-layouter to be used by the view.
     *
     * @param connectionLayouter the graph editor's {@link ConnectionLayouter}
     * instance
     */
    public void setConnectionLayouter(final ConnectionLayouter connectionLayouter) {
        this.connectionLayouter = connectionLayouter;
    }

    /**
     * Clears all elements from the view.
     */
    public void clear() {
        nodeLayer.getChildren().clear();
        connectionLayer.getChildren().clear();
    }

    /**
     * Adds a node skin to the view.
     *
     * @param nodeSkin the {@link GNodeSkin} instance to be added
     */
    public void add(final GNodeSkin nodeSkin) {
        if (nodeSkin != null) {
            nodeLayer.getChildren().add(nodeSkin.getRoot());
        }
    }

    /**
     * Adds a connection skin to the view.
     *
     * @param connectionSkin the {@link GConnectionSkin} instance to be added
     */
    public void add(final GConnectionSkin connectionSkin) {
        if (connectionSkin != null) {
            connectionLayer.getChildren().add(connectionSkin.getRoot());
        }
    }

    /**
     * Adds a joint skin to the view.
     *
     * @param jointSkin the {@link GJointSkin} instance to be added
     */
    public void add(final GJointSkin jointSkin) {
        if (jointSkin != null) {
            connectionLayer.getChildren().add(jointSkin.getRoot());
        }
    }

    /**
     * Adds a tail skin to the view.
     *
     * @param tailSkin the {@link GTailSkin} instance to be added
     */
    public void add(final GTailSkin tailSkin) {
        if (tailSkin != null) {
            connectionLayer.getChildren().add(tailSkin.getRoot());
        }
    }

    /**
     * Removes a node skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param nodeSkin the {@link GNodeSkin} instance to remove
     */
    public void remove(final GNodeSkin nodeSkin) {
        if (nodeSkin != null) {
            nodeLayer.getChildren().remove(nodeSkin.getRoot());
        }
    }

    /**
     * Removes a connection skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param connectionSkin the {@link GConnectionSkin} instance to remove
     */
    public void remove(final GConnectionSkin connectionSkin) {
        if (connectionSkin != null) {
            connectionLayer.getChildren().remove(connectionSkin.getRoot());
        }
    }

    /**
     * Removes a joint skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param jointSkin the {@link GJointSkin} instance to remove
     */
    public void remove(final GJointSkin jointSkin) {
        if (jointSkin != null) {
            connectionLayer.getChildren().remove(jointSkin.getRoot());
        }
    }

    /**
     * Removes a tail skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param tailSkin the {@link GTailSkin} instance to remove
     */
    public void remove(final GTailSkin tailSkin) {
        if (tailSkin != null) {
            connectionLayer.getChildren().remove(tailSkin.getRoot());
        }
    }

    /**
     * Sets the layout properties of the view.
     *
     * <p>
     * This is used specify information like whether the grid should be shown
     * and/or snapped to.
     * </p>
     *
     * @param editorProperties the {@link GraphEditorProperties} instance to be
     * used by the view
     */
    public void setEditorProperties(final GraphEditorProperties editorProperties) {

        this.editorProperties = editorProperties;
        grid.setProperties(editorProperties);
    }

    /**
     * Gets the editor properties instance used by the view.
     *
     * @return editorProperties the {@link GraphEditorProperties} instance used
     * by the view
     */
    public GraphEditorProperties getEditorProperties() {
        return editorProperties;
    }

    /**
     * Draws a selection box in the view.
     *
     * @param x the x position of the selection box
     * @param y the y position of the selection box
     * @param width the width of the selection box
     * @param height the height of the selection box
     */
    public void drawSelectionBox(final double x, final double y, final double width, final double height) {
        selectionBox.draw(x, y, width, height);
    }

    /**
     * Hides the selection box.
     */
    public void hideSelectionBox() {
        selectionBox.setVisible(false);
    }

    /**
     * Enables / disables caching of the view content (node and connection
     * layers).
     *
     * <p>
     * This increases performance if the content does not need to be redrawn. It
     * <b>decreases</b> performance when the content is redrawn. Use with care.
     * </p>
     *
     * <p>
     * <b>Note:</b> Currently leads to poor performance when scale transforms
     * are used, or on retina displays.
     * </p>
     *
     * @param cache {@code true} to enable caching, {@code false} to disable it
     */
    public void setContentCache(final boolean cache) {
        nodeLayer.setCache(cache);
        connectionLayer.setCache(cache);
        grid.setCache(cache);
    }

    @Override
    protected void layoutChildren() {
        final double width = getWidth();
        final double height = getHeight();
        nodeLayer.resizeRelocate(0, 0, width, height);
        connectionLayer.resizeRelocate(0, 0, width, height);
        connectionLayouter.redraw();
        grid.resizeRelocate(0, 0, width, height);
    }

    /**
     * Initializes the two layers (node and connection) that the view is
     * composed of.
     */
    private void initializeLayers() {

        nodeLayer.setPickOnBounds(false);
        connectionLayer.setPickOnBounds(false);

        nodeLayer.setCacheHint(CacheHint.SPEED);
        connectionLayer.setCacheHint(CacheHint.SPEED);

        nodeLayer.getStyleClass().add(STYLE_CLASS_NODE_LAYER);
        connectionLayer.getStyleClass().add(STYLE_CLASS_CONNECTION_LAYER);

        nodeLayer.setId(NODE_LAYER_ID);
        connectionLayer.setId(CONNECTION_LAYER_ID);

        nodeLayer.maxWidthProperty().bind(maxWidthProperty());
        nodeLayer.maxHeightProperty().bind(maxHeightProperty());
        nodeLayer.minWidthProperty().bind(minWidthProperty());
        nodeLayer.minHeightProperty().bind(minHeightProperty());

        connectionLayer.maxWidthProperty().bind(maxWidthProperty());
        connectionLayer.maxHeightProperty().bind(maxHeightProperty());
        connectionLayer.minWidthProperty().bind(minWidthProperty());
        connectionLayer.minHeightProperty().bind(minHeightProperty());

        // Node layer should be on top of connection layer, so we add it second.
        getChildren().addAll(grid, connectionLayer, nodeLayer, selectionBox);
    }
}
