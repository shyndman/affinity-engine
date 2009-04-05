package ca.scotthyndman.game.prototype.state;

import java.util.concurrent.Callable;

import ca.scotthyndman.game.prototype.GameMain;
import ca.scotthyndman.game.prototype.entity.Env;
import ca.scotthyndman.game.prototype.scene.Graphic;
import ca.scotthyndman.game.prototype.scene.RootNode;

import com.jme.input.InputHandler;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.Timer;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

public class InGameState extends BasicGameState {

	/**
	 * Display system.
	 */
	private DisplaySystem display = DisplaySystem.getDisplaySystem();

	/**
	 * The input handler.
	 */
	private InputHandler input;

	/**
	 * Information about the state of the game and environment that is passed to update methods.
	 */
	private Env gameEnv;

	/**
	 * Constructs a new {@link InGameState}.
	 */
	public InGameState(String name, TransitionGameState transition) {
		super(name);

		// 1. SET UP THE ROOT NODE

		buildRootNode();
		transition.increment();

		// 2. INITIALIZE INPUT

		initInput();
		transition.increment();

		// 3. BUILD INITIAL SCENE

		buildInitialScene();
		transition.increment();

		// 4. UPDATE THE ROOT NODE

		updateRootNode();
		transition.increment();
	}

	Graphic g;

	private void buildRootNode() {
		rootNode = new RootNode();
		ZBufferState buf = display.getRenderer().createZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		rootNode.setRenderState(buf);

		g = new Graphic("virus.png", 32, 32);
		//g.centerX.set(100);
//		g.centerY.set(100);
//		System.out.println("top: " + g.getTopNode().getWorldTranslation());
//		System.out.println("content: " + g.getContentNode().getWorldTranslation());
//		System.out.println("graphic: " + g.getContentNode().getChild(0).getWorldTranslation());
		rootNode.attachChild(g.getTopNode());
//		g.centerY.animateTo(200, 6000);
//		g.angle.animateTo(720, 3000);
		g.alpha.animate(1, 0, 5000);
//		g.scaleX.animateTo(3f, 3000);
//		g.scaleY.animateTo(3f, 3000, null, 1000);
//		g.x.animateTo(700f, 4000);
	}

	private void initInput() {
		input = new InputHandler();
	}

	private void buildInitialScene() {
		gameEnv = new Env();
	}

	private void updateRootNode() {
		rootNode.updateRenderState();
		rootNode.updateGeometricState(0, true);
	}

	/**
	 * Set the background color to Black. Hide the mouse cursor.
	 * 
	 * @param active
	 *            GameState aktiv Yes/no
	 */
	@Override
	public final void setActive(final boolean active) {
		super.setActive(active);

		GameTaskQueueManager.getManager().update(new Callable<Object>() {
			public Object call() throws Exception {
				GameMain.resume();
				return null;
			}
		});

		// Reset timer
		Timer.getTimer().reset();

		// Set background color
		Callable<Object> exe = new Callable<Object>() {
			public Object call() {
				display.getRenderer().setBackgroundColor(ColorRGBA.red);
				return null;
			}
		};

		// Render
		GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).enqueue(exe);
	}

	/**
	 * the update Method gets called once per Frame. Spawn Random Asteroids with the Earth as target. Updates the
	 * position of the skybox/stardust. Updates the chasecam.
	 * 
	 * @param tpf
	 *            time since last frame in ms.
	 */
	@Override
	public final void update(float tpf) {
		if (GameMain.isPaused()) {
			tpf = 0;
		}

		input.update(tpf);
		g.update(gameEnv, tpf);
		rootNode.updateGeometricState(tpf, true);
		
		
	}

	/**
	 * Render the Scene and draw the HUD.
	 */
	@Override
	public void render(float tpf) {
		if (GameMain.isPaused()) {
			tpf = 0;
		}
		super.render(tpf);
	}

}
