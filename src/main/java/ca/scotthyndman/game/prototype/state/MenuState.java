package ca.scotthyndman.game.prototype.state;

import java.util.concurrent.Callable;

import org.fenggui.Button;
import org.fenggui.CheckBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.binding.render.Binding;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.composite.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.layout.GridLayout;
import org.fenggui.theme.ITheme;
import org.fenggui.theme.XMLTheme;

import ca.scotthyndman.game.prototype.GameMain;
import ca.scotthyndman.game.prototype.input.menu.MenuInputHandler;

import com.jme.image.Texture;
import com.jme.input.MouseInput;
import com.jme.math.Vector3f;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.TransitionGameState;

public class MenuState extends BasicGameState {
	private int width = DisplaySystem.getDisplaySystem().getWidth();
	private int height = DisplaySystem.getDisplaySystem().getHeight();

	private Window settingsFrame = null;
	private ITheme theme;

	/**
	 * FengGUI display.
	 */
	private Display disp = null;

	/**
	 * FengGUI InputHandler.
	 */
	private MenuInputHandler input = null;

	/**
	 * Constructor, builds the FengGUI Menu.
	 * 
	 * @param name
	 *            Name of the GameStates
	 */
	public MenuState(final String name, final TransitionGameState trans) {
		super(name);
		buildUI(trans);
	}

	/**
	 * creates the Menu.
	 */
	@SuppressWarnings("deprecation")
	private void buildUI(final TransitionGameState trans) {
		Callable<Object> call = new Callable<Object>() {
			public Object call() throws Exception {
				disp = new Display(new LWJGLBinding());
				try {
					Binding.getInstance().setUseClassLoader(true);
					theme = new XMLTheme("themes/QtCurve.txt");
				} catch (Exception e1) {
					e1.printStackTrace();
					GameMain.shutDown();
				}
				return null;
			}
		};
		try {
			GameTaskQueueManager.getManager().render(call).get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		trans.increment();

		FengGUI.setTheme(theme);

		input = new MenuInputHandler(disp);
		Window frame = FengGUI.createWindow(disp, false, false, false, false);
		frame.setSize(300, 200);
		frame.setXY(width / 2 - frame.getWidth() / 2, height / 2 - frame.getHeight() / 2);

		frame.setTitle("Stardust Menu");
		frame.setLayoutManager(new GridLayout(6, 1));

		Button intro = FengGUI.createButton(frame, "Credits");
		intro.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(final ButtonPressedEvent e) {
				if (isActive()) {
					GameStateManager.getInstance().deactivateAllChildren();
					GameStateManager.getInstance().activateChildNamed("Intro");
				}
			}
		});

		Button start = FengGUI.createButton(frame, "Play");
		start.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(final ButtonPressedEvent e) {
				if (isActive()) {
					GameStateManager.getInstance().deactivateAllChildren();
					GameStateManager.getInstance().activateChildNamed("InGame");
				}
			}
		});

		Button settings = FengGUI.createButton(frame, "Settings");
		settings.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(final ButtonPressedEvent e) {
				createSettingsFrame();
			}
		});

		Button quit = FengGUI.createButton(frame, "Exit");
		quit.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(final ButtonPressedEvent e) {
				if (isActive()) {
					GameMain.shutDown();
				}
			}
		});

		// frame.pack();

		disp.layout();
	}

	/**
	 * activate / deactivate Mouse cursor.
	 * 
	 * @param active
	 *            active yes/no.
	 */
	@Override
	public final void setActive(final boolean active) {
		super.setActive(active);
		if (active == false) {
			MouseInput.get().setCursorVisible(false);
			input.setEnabled(false);
			return;
		}
		input.setEnabled(true);
		MouseInput.get().setCursorVisible(true);

		// reset any texture transformations, FengGUI would inherit them
		final TextureState defaultTextureState;
		Texture defTex = TextureState.getDefaultTexture().createSimpleClone();
		defTex.setScale(new Vector3f(1, 1, 1));
		defaultTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		defaultTextureState.setTexture(defTex);

		GameTaskQueueManager.getManager().render(new Callable<Object>() {
			public Object call() throws Exception {
				defaultTextureState.apply();
				return null;
			}
		});
	}

	private void createSettingsFrame() {
		if (settingsFrame != null) {
			settingsFrame.setVisible(true);
			return;
		}
		GameTaskQueueManager.getManager().render(new Callable<Object>() {
			@SuppressWarnings("deprecation")
			public Object call() throws Exception {
				settingsFrame = FengGUI.createWindow(disp, true, false, false, false);
				settingsFrame.getCloseButton().addButtonPressedListener(new IButtonPressedListener() {
					public void buttonPressed(ButtonPressedEvent e) {
						settingsFrame.setVisible(false);
					}
				});
				settingsFrame.setSize(300, 100);
				settingsFrame.setXY(width / 4 - settingsFrame.getWidth() / 2, height / 2 - settingsFrame.getHeight()
						/ 2);

				settingsFrame.setTitle("Settings");
				settingsFrame.setLayoutManager(new GridLayout(4, 1));

				Container frame = FengGUI.createContainer(settingsFrame);
				frame.setLayoutManager(new GridLayout(3, 2));

				FengGUI.createLabel(frame, "invert pitch controls");

				CheckBox<String> checkInvertMouse = FengGUI.createCheckBox(frame);
				checkInvertMouse.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						// XXX ControlManager.get().invertPitch();
					}
				});

				FengGUI.createLabel(frame, "enable Sound Fx");

				CheckBox<String> soundEnabled = FengGUI.createCheckBox(frame);
				soundEnabled.setSelected(true);
				soundEnabled.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						// XXX SoundUtil.get().setEnableSoundFx(!SoundUtil.get().isEnableSoundFx());
					}
				});

				FengGUI.createLabel(frame, "enable Music");

				CheckBox<String> musicEnabled = FengGUI.createCheckBox(frame);
				musicEnabled.setSelected(true);
				musicEnabled.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						// XXX SoundUtil.get().setEnableMusic(!SoundUtil.get().isEnableMusic());
					}
				});

				// frame.pack();
				disp.layout();

				return null;
			}
		});
	}

	/**
	 * draw the FengGUI Menu.
	 * 
	 * @param tpf
	 *            time since last frame.
	 */
	@Override
	public final void render(final float tpf) {
		super.render(tpf);
		disp.display();
	}
}
