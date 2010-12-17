package ca.scotthyndman.game.engine.animation;

/**
 * The BindFunction class allows Properties to be bound to a function.
 */
public interface BindFunction<T> {

	/**
	 * The function.
	 * 
	 * @return an object used to set the value of the property this function is bound to.
	 */
	public T f();
}
