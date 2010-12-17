package ca.scotthyndman.game.prototype.state;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import ca.scotthyndman.game.engine.Engine;
import ca.scotthyndman.game.engine.entity.Env;
import ca.scotthyndman.game.engine.event.EventManager;
import ca.scotthyndman.game.engine.input.InputSystem;
import ca.scotthyndman.game.engine.scene.Graphic;
import ca.scotthyndman.game.engine.scene.Group;
import ca.scotthyndman.game.engine.scene.Positioned;
import ca.scotthyndman.game.engine.scene.RootNode;
import ca.scotthyndman.game.engine.scene.Updatable;
import ca.scotthyndman.game.engine.scene.UpdateManager;
import ca.scotthyndman.game.engine.scripting.ScriptEngine;

import com.jme.input.MouseInput;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.Timer;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

public class InGameStateOld extends BasicGameState implements UpdateManager {

	/**
	 * Display system.
	 */
	private DisplaySystem display = DisplaySystem.getDisplaySystem();

	/**
	 * The input system.
	 */
	private InputSystem input;

	/**
	 * The event manager.
	 */
	private EventManager eventManager;

	/**
	 * A list of updatables that are notified on every frame.
	 */
	private Set<Updatable> updatables = new HashSet<Updatable>(2000);;

	/**
	 * The script containing game logic.
	 */
	private String gameScript;
	
	/**
	 * Constructs a new {@link InGameStateOld}.
	 */
	public InGameStateOld(String name, String gameScript, TransitionGameState transition) {
		super(name);
		this.gameScript = gameScript;

		// 1. SET UP THE ROOT NODE

		buildRootNode();
		transition.increment();

		// 2. INITIALIZE INPUT

		initInput();
		transition.increment();

		// 3. START UP THE EVENT ENGINE

		initEvents();
		transition.increment();

		// 4. UPDATE THE ROOT NODE

		updateRootNode();
		transition.increment();

		// 5. START UP SCRIPTING SUPPORT

		initScripts();
		transition.increment();
	}

	Graphic g;

	private void buildRootNode() {
		rootNode = new RootNode(this);
		ZBufferState buf = display.getRenderer().createZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		rootNode.setRenderState(buf);
	}

	private void initInput() {
		input = new InputSystem();
		Env.getInstance().setInputState(input.getInputState());
	}

	private void initScripts() {
		Env.getInstance().setUpdateManager(this);
		ScriptEngine engine = Engine.getInstance().getScriptManager().getEngine();
		engine.event_manager(eventManager);
		engine.input_system(input);
		Env.getInstance().setRootNode((RootNode) rootNode);
//		ScriptManagerOld.getInstance().getEngine().ready(gameScript);
	}

	private void updateRootNode() {
		rootNode.updateRenderState();
		rootNode.updateGeometricState(0, true);
	}

	private void initEvents() {
		eventManager = new EventManager();
	}

	/**
	 * Set the background color to Black. Hide the mouse cursor.
	 * 
	 * @param active
	 *            EngineGameState aktiv Yes/no
	 */
	@Override
	public final void setActive(final boolean active) {
		super.setActive(active);
		GameTaskQueueManager.getManager().update(new Callable<Object>() {
			public Object call() throws Exception {
				Engine.resume();
				return null;
			}
		});

		// Reset timer
		Timer.getTimer().reset();

		// Set background color
		Callable<Object> exe = new Callable<Object>() {
			public Object call() {
				display.getRenderer().setBackgroundColor(ColorRGBA.white);
				return null;
			}
		};

		// Render
		GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).enqueue(exe);
	}

	//
	// ======== UPDATING AND UPDATABLES
	//

	/**
	 * the update Method gets called once per Frame. Spawn Random Asteroids with the Earth as target. Updates the
	 * position of the skybox/stardust. Updates the chasecam.
	 * 
	 * @param tpf
	 *            time since last frame in ms.
	 */
	@Override
	public final void update(float tpf) {
		MouseInput.get().setCursorVisible(true);

		if (Engine.isPaused()) {
			tpf = 0;
		}

		Env env = Env.getInstance();
		input.update(tpf);
		eventManager.update(tpf);
		synchronized (updatables) {
			for (Updatable updatable : updatables) {
				updatable.update(env, tpf);
			}
		}

		rootNode.updateGeometricState(tpf, true);
	}

	public void updatableWasAdded(Updatable updatable) {
		if (updatable instanceof Group) {
			groupWasAdded((Group) updatable);
			return;
		}
		updatables.add(updatable);
	}

	public void updatableWasRemoved(Updatable updatable) {
		if (updatable instanceof Group) {
			groupWasRemoved((Group) updatable);
			return;
		}
		updatables.remove(updatable);
	}

	void updatableWasAdded(Updatable updatable, boolean noTypeCheck) {
		updatables.add(updatable);
	}

	void updatableWasRemoved(Updatable updatable, boolean noTypeCheck) {
		updatables.remove(updatable);
	}

	public void groupWasAdded(Group updatable) {
		for (Positioned p : updatable) {
			if (p instanceof Group) {
				groupWasAdded((Group) p);
				continue;
			}

			updatableWasAdded(p, true);
		}
		updatableWasAdded(updatable, true);
	}

	public void groupWasRemoved(Group updatable) {
		for (Positioned p : updatable) {
			if (p instanceof Group) {
				groupWasRemoved((Group) p);
				continue;
			}

			updatableWasRemoved(p, true);
		}
		updatableWasRemoved(updatable, true);
	}

	//
	// ======== RENDERING
	//

	/**
	 * Render the Scene and draw the HUD.
	 */
	@Override
	public void render(float tpf) {
		if (Engine.isPaused()) {
			tpf = 0;
		}
		super.render(tpf);
	}

}
