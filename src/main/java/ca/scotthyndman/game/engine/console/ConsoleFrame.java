package ca.scotthyndman.game.engine.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;

import org.jruby.Ruby;

import ca.scotthyndman.game.engine.scripting.ScriptManager;

public class ConsoleFrame extends JFrame {

	private ScriptManager manager;
	private Ruby runtime;
	private ExecutorService scriptExecutorService;
	private JSplitPane nodeSplitter;
	private JScrollPane nodeTreeScroller;
	private JTree nodeTree;
	private JScrollPane nodePropertiesScroller;
	private SGTableModel nodePropertiesModel;
	private JTable nodeProperties;
	private NodePanelController nodePanelController;
	private JSplitPane mainSplitter;
	private JPanel consolePanel;
	private JScrollPane outputScroller;
	private JTextPane output;
	private JEditorPane editor;
	private EditorController editorController;
	private DefaultTreeCellRenderer nodeTreeRenderer;

	public ConsoleFrame(TreeModel model) {
		setTitle("Affinity Engine Debug Console");
		setSize(500, 600);

		//
		// Set the look and feel
		//
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		mainSplitter = new JSplitPane();
		nodeSplitter = new JSplitPane();
		nodeTreeScroller = new JScrollPane();
		nodeTree = new JTree();
		nodePropertiesScroller = new JScrollPane();
		nodeProperties = new JTable();
		consolePanel = new JPanel();
		outputScroller = new JScrollPane();
		output = new JTextPane();
		editor = new JEditorPane();
		nodePropertiesModel = new SGTableModel();
		nodePanelController = new NodePanelController(nodeTree, nodeProperties, nodePropertiesModel);
		nodeTreeRenderer = new DefaultTreeCellRenderer();
		
		//
		// Font
		//
		Font font = findFont("Monospaced", Font.PLAIN, 14, new String[] { "Monaco", "Andale Mono" });

		//
		// Icons
		//
		ImageIcon branchIcon = new ImageIcon(getClass().getResource("resources/branch.png"));
		ImageIcon leafIcon = new ImageIcon(getClass().getResource("resources/leaf.png"));
		nodeTreeRenderer.setOpenIcon(branchIcon);
		nodeTreeRenderer.setClosedIcon(branchIcon);
		nodeTreeRenderer.setLeafIcon(leafIcon);

		mainSplitter.setDividerLocation(200);
		mainSplitter.setDividerSize(5);
		mainSplitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		mainSplitter.setName("splitter"); // NOI18N

		nodeSplitter.setDividerLocation(200);
		nodeSplitter.setName("nodeSplitter"); // NOI18N
		nodeSplitter.setResizeWeight(2f/5);
		nodeTreeScroller.setName("nodeTreeScroller"); // NOI18N
		nodeTreeScroller.setPreferredSize(new java.awt.Dimension(300, 0));
		nodeTree.setName("nodeTree"); // NOI18N
		nodeTree.setRequestFocusEnabled(false);
		nodeTree.setModel(model);
		nodeTree.setCellRenderer(nodeTreeRenderer);
		nodeTree.addTreeSelectionListener(nodePanelController);
		nodeTree.setShowsRootHandles(true);
		nodeTree.setFont(font);
		nodeTreeScroller.setViewportView(nodeTree);
		nodeSplitter.setLeftComponent(nodeTreeScroller);
		nodePropertiesScroller.setName("nodePropertiesScroller"); // NOI18N
		nodePropertiesScroller.setPreferredSize(new java.awt.Dimension(100, 0));
		nodeProperties.setModel(nodePropertiesModel);
		nodeProperties.setName("nodeProperties"); // NOI18N
		nodeProperties.setFont(font);
		//nodeProperties.setPreferredSize(new java.awt.Dimension(100, 200));
		nodeProperties.setRequestFocusEnabled(false);
		nodePropertiesScroller.setViewportView(nodeProperties);
		nodeSplitter.setRightComponent(nodePropertiesScroller);
		mainSplitter.setTopComponent(nodeSplitter);

		consolePanel.setPreferredSize(new java.awt.Dimension(200, 20));
		consolePanel.setLayout(new BorderLayout());
		outputScroller.setName("outputScroller"); // NOI18N
		output.setEditable(false);
		output.setDocument(new DefaultStyledDocument());
		output.setFont(font);
		output.setMargin(new Insets(4, 4, 4, 4));
		output.setText("Loading\n");
		outputScroller.setViewportView(output);

		editor.setName("editor"); // NOI18N
		editor.setPreferredSize(new java.awt.Dimension(200, 16));
		editor.setDocument(new DefaultStyledDocument());
		editor.setCaretColor(new Color(0xa4, 0x00, 0x00));
		editor.setBackground(new Color(0xf2, 0xf2, 0xf2));
		editor.setForeground(new Color(0xa4, 0x00, 0x00));
		editor.setFont(font);

		consolePanel.add(outputScroller, BorderLayout.CENTER);
		consolePanel.add(editor, BorderLayout.SOUTH);
		mainSplitter.setBottomComponent(consolePanel);

		//
		// Build the editor editorController
		//
		editorController = new EditorController(editor, output, outputScroller);
		editor.addKeyListener(editorController);

		//
		// Set the content and display
		//
		setContentPane(mainSplitter);
		setVisible(true);
		editor.requestFocus();
	}

	public void initialize(ScriptManager manager) {
		this.manager = manager;
		this.runtime = manager.getRuntime();
		this.scriptExecutorService = manager.getScriptExecutorService();
		editorController.initialize(manager);
		output.setText("Loaded!\n");
	}

	public EditorController getEditorController() {
		return editorController;
	}

	/**
	 * Finds a font.
	 */
	private Font findFont(String otherwise, int style, int size, String[] families) {
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		Arrays.sort(fonts);
		Font font = null;
		for (int i = 0; i < families.length; i++) {
			if (Arrays.binarySearch(fonts, families[i]) >= 0) {
				font = new Font(families[i], style, size);
				break;
			}
		}
		if (font == null)
			font = new Font(otherwise, style, size);
		return font;
	}
}
