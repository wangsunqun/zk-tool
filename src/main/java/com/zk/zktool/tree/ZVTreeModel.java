/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.zk.zktool.tree;

import com.zk.zktool.model.ZVModel;
import com.zk.zktool.model.ZVModelImpl;
import com.zk.zktool.model.ZVModelListener;
import com.zk.zktool.node.ZVNode;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.StringTokenizer;

public class ZVTreeModel implements TreeModel {
    /**
     * Listeners.
     */
    protected EventListenerList listenerList = new EventListenerList();
    private final ZVModel model;

    public ZVTreeModel(ZVModel model) {
        this.model = model;
        model.addModelListener(new ZVModelListener() {


            public void nodeDeleted(ZVNode oldNode, int oldIndex) {
                System.out.println("nodeDeleted : " + oldNode);
                ZVTreeModel.this.fireTreeNodesRemoved(this, ZVTreeModel.this
                                .getTreePath(oldNode).getParentPath(),
                        new int[]{oldIndex}, new Object[]{oldNode});
            }


            public void nodeDataChanged(ZVNode node) {
                System.out.println("nodeDataChanged : " + node);
                // FLE+
                System.out.println("nodeDataChanged : " + node);
                TreePath parentPath = ZVTreeModel.this.getTreePath(node)
                        .getParentPath();
                int index = ZVTreeModel.this.getIndexOfChild(
                        parentPath.getLastPathComponent(), node);
                ZVTreeModel.this.fireTreeNodesChanged(this,
                        parentPath.getPath(), new int[]{index},
                        new Object[]{node});
            }


            public void nodeCreated(ZVNode newNode) {
                if (newNode == ZVTreeModel.this.getRoot()) {
                    ZVTreeModel.this.fireTreeStructureChanged(this,
                            new TreePath(newNode));
                } else {
                    try {
                        TreePath treePath = ZVTreeModel.this.getTreePath(
                                newNode).getParentPath();
                        ZVTreeModel.this.fireTreeNodesInserted(this, treePath,
                                new int[]{ZVTreeModel.this.getIndexOfChild(
                                        treePath.getLastPathComponent(),
                                        newNode)}, new Object[]{newNode});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public TreePath getTreePath(ZVNode node) {
        String path = node.getPath();
        TreePath treePath = new TreePath(this.model.getNode("/"));
        String currentPath = "";
        StringTokenizer st = new StringTokenizer(path, "/", false);
        while (st.hasMoreTokens()) {
            currentPath += "/" + st.nextToken();
            treePath = treePath.pathByAddingChild(this.model
                    .getNode(currentPath));
        }
        return treePath;
    }


    public ZVNode getRoot() {
        return this.model.getNode("/");
    }


    public ZVNode getChild(Object parent, int index) {
        if (!(parent instanceof ZVNode)) {
            throw new IllegalArgumentException("parent must be a ZVNode");
        }

        return this.model.getChildren((ZVNode) parent).get(index);
    }


    public int getChildCount(Object parent) {
        if (!(parent instanceof ZVNode)) {
            throw new IllegalArgumentException("parent must be a ZVNode");
        }

        return this.model.getChildren((ZVNode) parent).size();
    }


    public boolean isLeaf(Object node) {
        if (!(node instanceof ZVNode)) {
            throw new IllegalArgumentException("node must be a ZVNode");
        }

        return this.model.getChildren((ZVNode) node).size() == 0;
    }


    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Can't change data");
    }


    public int getIndexOfChild(Object parent, Object child) {
        if (!(parent instanceof ZVNode)) {
            throw new IllegalArgumentException("parent must be a ZVNode");
        }
        if (!(child instanceof ZVNode)) {
            throw new IllegalArgumentException("child must be a ZVNode");
        }
        return this.model.getChildren((ZVNode) parent).indexOf(child);
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @param l the listener to add
     * @see #removeTreeModelListener
     */

    public void addTreeModelListener(TreeModelListener l) {
        this.listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @param l the listener to remove
     * @see #addTreeModelListener
     */

    public void removeTreeModelListener(TreeModelListener l) {
        this.listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @param source       the node being changed
     * @param path         the path to the root node
     * @param childIndices the indices of the changed elements
     * @param children     the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(Object source, Object[] path,
                                        int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @param source       the node where new elements are being inserted
     * @param path         the path to the root node
     * @param childIndices the indices of the new elements
     * @param children     the new elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, TreePath path,
                                         int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    protected void fireTreeNodesRemoved(Object source, TreePath treePath,
                                        int[] indexes, Object[] objects) {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, treePath, indexes, objects);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @param source       the node where the tree model has changed
     * @param path         the path to the root node
     * @param childIndices the indices of the affected elements
     * @param children     the affected elements
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, Object[] path,
                                            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /*
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @param source the node where the tree model has changed
     *
     * @param path the path to the root node
     *
     * @see EventListenerList
     */
    private void fireTreeStructureChanged(Object source, TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

}
