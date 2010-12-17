package ca.scotthyndman.game.engine.state;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import ca.scotthyndman.game.engine.Engine;
import ca.scotthyndman.game.engine.entity.Env;
import ca.scotthyndman.game.engine.event.EventManager;
import ca.scotthyndman.game.engine.input.InputSystem;
import ca.scotthyndman.game.engine.scene.Graphic;
import ca.scotthyndman.game.engine.scene.Group;
import ca.scotthyndman.game.engine.scene.Positioned;
import ca.scotthyndman.game.engine.scene.RootNode;
import ca.scotthyndman.game.engine.scene.SGEvent;
import ca.scotthyndman.game.engine.scene.Updatable;
import ca.scotthyndman.game.engine.scene.UpdateManager;
import ca.scotthyndman.game.engine.scripting.ScriptEngine;
import ca.scotthyndman.game.engine.scripting.ScriptManager;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.Timer;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

/**
 * Represents being in the game.
 * 
 * @author scottyhyndman
 */
public class InGameState extends BasicGameState implements UpdateManager {

	/**
	 * Display system.
	 */
	private DisplaySystem display = DisplaySystem.getDisplaySystem();

	/**
	 * The input system.
	 */
	private InputSystem input;

	/**
	 * The script manager.
	 */
	private ScriptManager scriptManager;

	/**
	 * The event manager.
	 */
	private EventManager eventManager;

	/**
	 * A list of updatables that are notified on every frame.
	 */
	private Vector<Updatable> updatables = new Vector<Updatable>(2000);;

	/**
	 * The script containing game logic.
	 */
	private String gameScript;

	/**
	 * Constructs a new {@link InGameState}.
	 */
	public InGameState(String name, String gameScript, TransitionGameState transition) {
		super(name);
		this.gameScript = gameScript;
		this.eventManager = Engine.getInstance().getEventManager();
		this.scriptManager = Engine.getInstance().getScriptManager();

		// 1. SET UP THE ROOT NODE

		buildRootNode();
		transition.increment();

		// 2. INITIALIZE INPUT

		initInput();
		transition.increment();

		// // 3. START UP THE EVENT ENGINE
		//
		// initEvents();
		// transition.increment();

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
		Env.getInstance().setRootNode((RootNode) rootNode);

		ScriptEngine engine = scriptManager.getEngine();
		engine.event_manager(eventManager);
		engine.input_system(input);

		scriptManager.runGame(this, gameScript);
	}

	private void updateRootNode() {
		rootNode.updateRenderState();
		rootNode.updateGeometricState(0, true);
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
		if (Engine.isPaused()) {
			tpf = 0;
		}

		Env env = Env.getInstance();
		input.update(tpf);
		eventManager.update(tpf);
		synchronized (updatables) {
			int len = updatables.size();
			for (int i = 0; i < len; i++) {
				updatables.get(i).update(env, tpf);
			}
		}

		rootNode.updateGeometricState(tpf, true);
	}

	public void updatableWasAdded(Updatable updatable) {
		if (updatable instanceof Group) {
			groupWasAdded((Group) updatable, true);
			return;
		}

		synchronized (updatables) {
			updatables.add(updatable);
		}
		if (updatable instanceof Positioned && SGEvent.isRooted((Positioned) updatable)) {
			eventManager.dispatchEvent("nodeAdded", new SGEvent((Positioned) updatable));
		}
	}

	public void updatableWasRemoved(Updatable updatable) {
		if (updatable instanceof Group) {
			groupWasRemoved((Group) updatable, false);
			return;
		}
		synchronized (updatables) {
			updatables.remove(updatable);
		}

		if (updatable instanceof Positioned) {
			eventManager.dispatchEvent("nodeRemoved", new SGEvent((Positioned) updatable));
		}
	}

	void updatableWasAdded(Updatable updatable, boolean noTypeCheck) {
		synchronized (updatables) {
			updatables.add(updatable);
		}
	}

	void updatableWasRemoved(Updatable updatable, boolean noTypeCheck) {
		synchronized (updatables) {
			updatables.remove(updatable);
		}
	}

	public void groupWasAdded(Group updatable) {
		for (Positioned p : updatable) {
			if (p instanceof Group) {
				groupWasAdded((Group) p, true);
				continue;
			}

			updatableWasAdded(p, true);
		}
		updatableWasAdded(updatable, true);

		if (SGEvent.isRooted((Positioned) updatable)) {
			eventManager.dispatchEvent("nodeAdded", new SGEvent((Positioned) updatable));
		}
	}

	public void groupWasAdded(Group updatable, boolean noEvent) {
		for (Positioned p : updatable) {
			if (p instanceof Group) {
				groupWasAdded((Group) p, noEvent);
				continue;
			}

			updatableWasAdded(p, true);
		}
		updatableWasAdded(updatable, true);

		if (!noEvent && SGEvent.isRooted((Positioned) updatable)) {
			eventManager.dispatchEvent("nodeAdded", new SGEvent((Positioned) updatable));
		}
	}

	public void groupWasRemoved(Group updatable) {
		for (Positioned p : updatable) {
			if (p instanceof Group) {
				groupWasRemoved((Group) p, true);
				continue;
			}

			updatableWasRemoved(p, true);
		}
		updatableWasRemoved(updatable, true);

		eventManager.dispatchEvent("nodeRemoved", new SGEvent((Positioned) updatable));
	}

	public void groupWasRemoved(Group updatable, boolean noEvent) {
		for (Positioned p : updatable) {
			if (p instanceof Group) {
				groupWasRemoved((Group) p, noEvent);
				continue;
			}

			updatableWasRemoved(p, true);
		}
		updatableWasRemoved(updatable, true);

		if (!noEvent) {
			eventManager.dispatchEvent("nodeRemoved", new SGEvent((Positioned) updatable));
		}
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
