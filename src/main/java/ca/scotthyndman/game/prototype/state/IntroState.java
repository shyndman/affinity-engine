package ca.scotthyndman.game.prototype.state;

import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.input.KeyInput;
import com.jme.input.controls.GameControl;
import com.jme.input.controls.GameControlManager;
import com.jme.input.controls.binding.KeyboardBinding;
import com.jme.input.controls.controller.ActionChangeController;
import com.jme.input.controls.controller.ControlChangeListener;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Text;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.effects.transients.Fader;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameStateManager;

/**
 * The Intro Gamestate. Displays the Stardust logo and plays the intro music.
 */
public class IntroState extends BasicGameState {

    /**
     * Array of lines of the Intro text.
     */
    private ArrayList<String> introText = null;

    /**
     * intro text.
     */
    private Text text = null;

    /**
     * Fader for the intro logo.
     */
    private Fader fader = null;

    /**
     * last change of the intro text.
     */
    private float lastChange = 0;

    /**
     * index of the currently displayed intro text.
     */
    private int introIdx = 0;

    /**
     * constructor.
     * 
     * @param name
     *            name of the gamestate.
     */
    public IntroState(final String name) {
	super(name);

	// Get the display system
	DisplaySystem disp = DisplaySystem.getDisplaySystem();

	// Set the intro text.
	introText = new ArrayList<String>();
	introText.add("Prototype - a game made by Scott Hyndman");
	introText.add("started Friday April 3rd, 2009");
	introText.add("created with: jMonkeyEngine");

	// The logo should be 80% of the width
	int width = disp.getWidth() / 10 * 8;
	int heigth = width / 2;

	// Display the logo in the middle of the screen
	Quad q = new Quad("titel", width, heigth);
	q.setLocalTranslation(disp.getWidth() / 2, disp.getHeight() / 2, 0);
	TextureState ts = disp.getRenderer().createTextureState();
	ts.setTexture(TextureManager.loadTexture(
		ResourceLocatorTool.locateResource(
			ResourceLocatorTool.TYPE_TEXTURE, "loading_black.png"),
		Texture.MinificationFilter.BilinearNoMipMaps,
		Texture.MagnificationFilter.Bilinear));
	q.setRenderState(ts);
	q.setRenderQueueMode(Renderer.QUEUE_ORTHO);
	rootNode.attachChild(q);

	// Setup the fader for the Logo.
	fader = new Fader("Fader", disp.getWidth(), disp.getHeight(),
		ColorRGBA.black, 5);
	fader.setAlpha(0.0f);
	fader.setMode(Fader.FadeMode.FadeIn);
	rootNode.attachChild(fader);

	// Create and position the text.
	text = Text.createDefaultTextLabel("text", "");
	text.setLocalTranslation(new Vector3f(30, 170, 0));
	text.setLocalScale(1.5f);

	initControls();

	rootNode.attachChild(text);
	rootNode.updateRenderState();
    }

    /**
     * init keyboard controls.
     */
    private void initControls() {
	GameControlManager manager = new GameControlManager();
	GameControl next = manager.addControl("next");
	next.addBinding(new KeyboardBinding(KeyInput.KEY_SPACE));
	next.addBinding(new KeyboardBinding(KeyInput.KEY_ESCAPE));

	ActionChangeController next1 = new ActionChangeController(next,
		new ControlChangeListener() {
		    public void changed(final GameControl control,
			    final float oldValue, final float newValue,
			    final float time) {
			if (newValue == 1.0f) {
			    // 1.0f == true
			    GameStateManager.getInstance()
				    .deactivateAllChildren();
			    GameStateManager.getInstance().activateChildNamed(
				    "Menu");
			}
		    }
		});
	this.getRootNode().addController(next1);
    }

    /**
     * resets timer when entering the Gamestate.
     * 
     * @param active
     *            active yes/no.
     */
    @Override
    public final void setActive(final boolean active) {
	super.setActive(active);
	if (active == false) {
	} else {
	    text.print(introText.get(0));
	    fader.setAlpha(1.0f);
	    lastChange = Timer.getTimer().getTimeInSeconds();
	    introIdx = 0;
	}
	Timer.getTimer().reset();
    }

    /**
     * display and update the intro text.
     * 
     * @param tpf
     *            time since last frame.
     */
    @Override
    public final void update(final float tpf) {
	super.update(tpf);
	if (lastChange + 5 < Timer.getTimer().getTimeInSeconds()) {
	    if (introIdx < introText.size()) {
		text.print(introText.get(introIdx));
	    }
	    lastChange = Timer.getTimer().getTimeInSeconds();
	    introIdx++;
	}
    }
}