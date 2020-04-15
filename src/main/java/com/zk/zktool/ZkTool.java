package com.zk.zktool;

import com.zk.zktool.model.ZVModel;
import com.zk.zktool.model.ZVModelImpl;
import com.zk.zktool.node.JZVNode;
import com.zk.zktool.node.ZVNode;
import com.zk.zktool.tree.JZVTree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: sqwang
 * @Date:
 */
public class ZkTool {
    private static final String DEFAULT_CONNECTION_STRING = "10.21.33.28:12181";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String connexionString = null;
        if (args.length > 0) {
            connexionString = args[0];
        } else {
            connexionString = inputConnectionString(DEFAULT_CONNECTION_STRING);
            if (connexionString == null || "".equals(connexionString)) {
                System.exit(2);
            }
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        final JTextField search = new JTextField("search, ignoring case");
        final ZVModel model = new ZVModelImpl(connexionString);
        final JZVNode nodeView = new JZVNode(model);
        final JZVTree tree = new JZVTree(model);

        String editorViewtitle = String.format("%s - Editor View - ZooViewer",
                connexionString);

        final JFrame jfEditor = new JFrame(editorViewtitle);
        jfEditor.setName("zv_editor");
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(tree), nodeView);
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, search, splitPane);


        jfEditor.add(splitPane2);
        jfEditor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jfEditor.setSize(1800, 1000);
        jfEditor.setVisible(true);

        splitPane.setDividerLocation(0.4);
        splitPane2.setDividerLocation(30);

        search.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                search.setText("");
            }
        });

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String key = search.getText();
                    if (key != null && !"".equals(key)) {
                        findInTree(key.toLowerCase(), tree);
                    }
                }
            }
        });

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath[] selPaths = tree.getSelectionModel()
                        .getSelectionPaths();
                if (selPaths == null) {
                    return;
                }
                ZVNode[] nodes = new ZVNode[selPaths.length];
                for (int i = 0; i < selPaths.length; i++) {
                    nodes[i] = (ZVNode) selPaths[i].getLastPathComponent();
                }
                nodeView.setNodes(nodes);
            }
        });

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                                                          Object value, boolean sel, boolean expanded, boolean leaf,
                                                          int row, boolean hasFocus) {
                Component comp = super.getTreeCellRendererComponent(tree,
                        value, sel, expanded, leaf, row, hasFocus);
                if ((comp instanceof JLabel) && (value instanceof ZVNode)) {
                    ZVNode node = (ZVNode) value;
                    String text = node.getName();
                    byte[] data = node.getData();
                    if ((data != null) && (data.length > 0)) {
                        text += "=" + new String(data);
                    }
                    ((JLabel) comp).setText(text);
                    comp.validate();
                }
                return comp;
            }
        };

        tree.setCellRenderer(renderer);

        jfEditor.setVisible(true);
        jfEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void findInTree(String str, JZVTree tree) {
        Object root = tree.getModel().getRoot();
        TreePath treePath = new TreePath(root);

        List<TreePath> list = new ArrayList<TreePath>();
        findInPath(treePath, str, tree, list);

        if (list.size() != 0) {
            TreePath firstNode = list.get(0);
            tree.scrollPathToVisible(firstNode);
            TreePath[] treePaths = new TreePath[list.size()];
            tree.setSelectionPaths(list.toArray(treePaths));
        }
    }

    private static TreePath findInPath(TreePath treePath, String str, JZVTree tree, List<TreePath> list) {
        Object object = treePath.getLastPathComponent();
        if (object == null) {
            return null;
        }

        String value = object.toString().toLowerCase();
        if (value.contains(str)) {
            list.add(treePath);
            return treePath;
        } else {
            TreeModel model = tree.getModel();
            int n = model.getChildCount(object);
            for (int i = 0; i < n; i++) {
                Object child = model.getChild(object, i);
                TreePath path = treePath.pathByAddingChild(child);

                findInPath(path, str, tree, list);
            }
            return null;
        }
    }

    private static String inputConnectionString(String defaultString) {
        JOptionPane pane = new JOptionPane(
                "Enter the connection string",
                JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION);
        pane.setWantsInput(true);
        pane.setInputValue(defaultString);
        pane.setInitialValue(defaultString);
        pane.setInitialSelectionValue(defaultString);

        JDialog dialog = pane.createDialog(null,
                "ZooKeeper server connection");

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(2);
            }
        });


        dialog.setVisible(true);

        Object inputValue = pane.getInputValue();
        if (inputValue == null) {
            return null;
        }

        return (String) inputValue;
    }
}
