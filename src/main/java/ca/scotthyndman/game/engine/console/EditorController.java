package ca.scotthyndman.game.engine.console;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jruby.Ruby;

import ca.scotthyndman.game.engine.console.command.ClearOutputCommand;
import ca.scotthyndman.game.engine.console.command.Command;
import ca.scotthyndman.game.engine.console.command.ExecutionResult;
import ca.scotthyndman.game.engine.console.command.ReloadCommand;
import ca.scotthyndman.game.engine.console.command.RunRubyCommand;
import ca.scotthyndman.game.engine.scripting.ScriptManager;

public class EditorController implements KeyListener {

	/**
	 * The runtime.
	 */
	private Ruby runtime;

	/**
	 * The script manager.
	 */
	private ScriptManager manager;

	/**
	 * The executor used to run scripts.
	 */
	private ExecutorService scriptExecutorService;

	/**
	 * Code completion.
	 */
	private InputCompleter completer;

	/**
	 * A map of commands to their associated command objects.
	 */
	private Map<String, Command> commandMap = new HashMap<String, Command>();

	/**
	 * The command that is run by default.
	 */
	private Command defaultCommand = new RunRubyCommand();

	/**
	 * History.
	 */
	private List<String> history = new LinkedList<String>();

	/**
	 * The current offset in the history list.
	 */
	private int currentHistoryOffset = 0;

	/**
	 * The current command.
	 */
	private int commandCount = 0;

	/**
	 * String formatter
	 */
	private Formatter formatter = new Formatter();

	/**
	 * The text editor.
	 */
	private JEditorPane editor;

	/**
	 * The output text field.
	 */
	private JTextPane output;

	/**
	 * The scroller for the output
	 */
	private JScrollPane outputScroller;

	public volatile MutableAttributeSet promptStyle;
	public volatile MutableAttributeSet inputStyle;
	public volatile MutableAttributeSet outputStyle;
	public volatile MutableAttributeSet errorStyle;

	private JComboBox completeCombo;
	private BasicComboPopup completePopup;

	//
	// ======== CONSTRUCTION
	//

	public EditorController(JEditorPane editor, JTextPane output, JScrollPane outputScroller) {
		this(editor, output, outputScroller, null);
	}

	public EditorController(JEditorPane editor, JTextPane output, JScrollPane outputScroller, final String message) {
		this.editor = editor;
		this.output = output;
		this.completer = new InputCompleter();
		this.outputScroller = outputScroller;
		this.initCommandMap();

		promptStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(promptStyle, new Color(0x00, 0x00, 0x66));

		inputStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(inputStyle, new Color(0x20, 0x4a, 0x87));

		outputStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(outputStyle, Color.darkGray);

		errorStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(errorStyle, new Color(0x99, 0x00, 0x00));

		completeCombo = new JComboBox();
		completeCombo.setRenderer(new DefaultListCellRenderer()); // no silly ticks!
		completePopup = new BasicComboPopup(completeCombo);

		if (message != null) {
			setText(message, inputStyle);
		}
	}

	//
	// ======== INITIALIZATION
	//

	/**
	 * Initializes the command map.
	 */
	private void initCommandMap() {
		commandMap.put("clear", new ClearOutputCommand());
		commandMap.put("reload", new ReloadCommand());
	}

	/**
	 * Initializes the controller.
	 */
	public void initialize(ScriptManager manager) {
		this.manager = manager;
		this.runtime = manager.getRuntime();
		this.scriptExecutorService = manager.getScriptExecutorService();
	}

	//
	// ======== PROPERTIES
	//

	public ExecutorService getScriptExecutorService() {
		return scriptExecutorService;
	}

	public ScriptManager getManager() {
		return manager;
	}

	//
	// ======== SETTING THE TEXT
	//

	/**
	 * Gets the editor's text.
	 */
	public String getText() {
		try {
			return editor.getDocument().getText(0, editor.getDocument().getLength());
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Clears then sets the text.
	 */
	public void setText(String text, AttributeSet style) {
		clearText();
		try {
			if (text != null) {
				editor.getDocument().insertString(0, text, style);
			}
		} catch (BadLocationException e) {
		}
	}

	public void replaceText(int start, int end, String text) {
		try {
			if (text != null) {
				editor.getDocument().remove(start, end - start);
				editor.getDocument().insertString(start, text, inputStyle);
			}
		} catch (BadLocationException e) {
		}
	}

	/**
	 * Clears the editor's text.
	 */
	public void clearText() {
		try {
			editor.getDocument().remove(0, editor.getDocument().getLength());
		} catch (BadLocationException e) {
		}
	}

	/**
	 * Clears the output.
	 */
	public void clearOuput() {
		try {
			output.getDocument().remove(0, output.getDocument().getLength());
		} catch (BadLocationException e) {
		}
	}

	/**
	 * Prints a line to the execution result.
	 */
	public void printResult(String commandString, ExecutionResult result) {
		if (result == null) {
			return;
		}

		StringBuilder stringBuilder = (StringBuilder) formatter.out();
		stringBuilder.delete(0, stringBuilder.length());
		printLine(formatter.format("%03d> %s", commandCount, commandString).toString(), promptStyle);
		printLine(result.resultString == null ? null : "=> " + result.resultString, result.isError ? errorStyle
				: outputStyle);
	}

	/**
	 * Prints a line to the execution result.
	 */
	public void printLine(String text) {
		printLine(text, outputStyle);
	}

	/**
	 * Prints a line to the execution result.
	 */
	public void printLine(String text, AttributeSet style) {
		if (text == null || text.length() == 0) {
			return;
		}

		try {
			output.getDocument().insertString(output.getDocument().getLength(), text + "\n", style);
			scrollToEnd();
		} catch (BadLocationException e) {
		}
	}

	//
	// ======== SCROLLING
	//

	private void scrollToEnd() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (isAdjusting()) {
					return;
				}

				int height = output.getHeight();
				output.scrollRectToVisible(new Rectangle(0, height - 1, 1, height));
			}
		});

	}

	private boolean isAdjusting() {
		JScrollBar scrollBar = outputScroller.getVerticalScrollBar();

		if (scrollBar != null && scrollBar.getValueIsAdjusting()) {
			return true;
		}

		return false;

	}

	//
	// ======== HISTORY
	//

	/**
	 * Gets the command given a history offset.
	 */
	private String getCommandForHistoryOffset(int offset) {
		if (offset == 0) {
			return "";
		}

		return history.get(history.size() + offset);
	}

	//
	// ======== KEY LISTENER
	//

	/**
	 * Runs the command
	 */
	private void enterAction(KeyEvent event) {
		event.consume();

		int start = getText().lastIndexOf('.') + 1;
		int end = editor.getCaretPosition();

		if (completePopup.isVisible()) {
			if (completeCombo.getSelectedItem() != null)
				replaceText(start, end, (String) completeCombo.getSelectedItem());
			completePopup.setVisible(false);
			return;
		}

		//
		// Get the command, and reset the editor state
		//
		String commandString = getText().trim();
		clearText();
		currentHistoryOffset = 0;
		commandCount++;

		//
		// Do nothing if no command
		//
		if (commandString.length() == 0) {
			return;
		}

		//
		// Add to history
		//
		history.add(commandString);

		//
		// Get the first word in the command
		//
		String[] commandParts = commandString.split("\\s+");
		String command = commandParts[0];

		//
		// See if we match against a registered command
		//
		ExecutionResult result;
		Command c;
		if (commandMap.containsKey(command)) {
			c = commandMap.get(command);
		} else {
			c = defaultCommand;
		}
		FutureTask<ExecutionResult> futureResult = new FutureTask<ExecutionResult>(
				new CommandCallable(c, commandString));
		scriptExecutorService.execute(futureResult);
		try {
			result = futureResult.get();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		//
		// Append the execution result to the output
		//
		printResult(commandString, result);
	}

	/**
	 * Goes back in the history by one, if possible.
	 */
	private void upAction(KeyEvent event) {
		event.consume();

		if (completePopup.isVisible()) {
			int selected = completeCombo.getSelectedIndex() - 1;
			if (selected < 0)
				return;
			completeCombo.setSelectedIndex(selected);
			return;
		}

		if (history.size() + currentHistoryOffset == 0) {
			beep();
			return;
		}

		currentHistoryOffset--;
		setText(getCommandForHistoryOffset(currentHistoryOffset), inputStyle);
	}

	/**
	 * Goes forward in the history by one, if possible.
	 */
	private void downAction(KeyEvent event) {
		event.consume();

		//
		// Keyboard navigation
		//
		if (completePopup.isVisible()) {
			int selected = completeCombo.getSelectedIndex() + 1;
			if (selected == completeCombo.getItemCount())
				return;
			completeCombo.setSelectedIndex(selected);
			return;
		}

		//
		// We're at the end
		//
		if (currentHistoryOffset == 0) {
			beep();
			return;
		}

		currentHistoryOffset++;
		setText(getCommandForHistoryOffset(currentHistoryOffset), inputStyle);
	}

	private void completeAction(KeyEvent event) {
		event.consume();

		if (completePopup.isVisible())
			return;

		String commandString = getText().trim();
		FutureTask<Iterable<Object>> completionResult = new FutureTask<Iterable<Object>>(new CompleterCallable(
				commandString));
		scriptExecutorService.execute(completionResult);
		Iterable<Object> completions;
		try {
			completions = completionResult.get();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Point pos = editor.getCaret().getMagicCaretPosition();
		if (pos == null)
			pos = new Point(0, 0);
		completeCombo.removeAllItems();
		int cnt = 0;
		for (Object c : completions) {
			completeCombo.addItem(c);
			cnt++;
		}

		if (cnt >= 10) {
			completePopup.getList().setVisibleRowCount(10);
		} else {
			completePopup.getList().setVisibleRowCount(cnt);
		}

		completePopup.show(editor, pos.x, pos.y + editor.getFontMetrics(editor.getFont()).getHeight());
	}

	public void keyPressed(KeyEvent event) {
		if (manager == null || !manager.isStarted()) {
			event.consume();
			// do nothing
			return;
		}

		int code = event.getKeyCode();
		switch (code) {
		case KeyEvent.VK_ENTER:
			enterAction(event);
			break;
		case KeyEvent.VK_UP:
			upAction(event);
			break;
		case KeyEvent.VK_DOWN:
			downAction(event);
			break;
		case KeyEvent.VK_TAB:
			completeAction(event);
			break;
		}

		if (completePopup.isVisible() && code != KeyEvent.VK_TAB && code != KeyEvent.VK_UP && code != KeyEvent.VK_DOWN) {
			completePopup.setVisible(false);
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}

	public void keyTyped(KeyEvent arg0) {
		if (manager == null || !manager.isStarted()) {
			arg0.consume();
			return;
		}
	}

	//
	// ======== DEBUGGING
	//

	private void beep() {
		System.out.println("beep");
	}

	//
	// ======== Command Future Task
	//

	private class CommandCallable implements Callable<ExecutionResult> {
		private Command command;
		private String input;

		public CommandCallable(Command command, String input) {
			this.command = command;
			this.input = input;
		}

		public ExecutionResult call() throws Exception {
			return this.command.execute(runtime, input, EditorController.this);
		}
	}

	private class CompleterCallable implements Callable<Iterable<Object>> {
		private String input;

		public CompleterCallable(String input) {
			this.input = input;
		}

		public Iterable<Object> call() throws Exception {
			return completer.doComplete(runtime, input);
		}
	}
}
