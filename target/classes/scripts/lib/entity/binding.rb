require '../const' if $0 == __FILE__ 

# Action bindings are a map of "game" actions to their associated input controls.
class ActionBindings  
  attr_accessor :mouse_binding, :key_bindings
  
  def initialize(mouse_binding, key_bindings) 
    @mouse_binding = mouse_binding;
    @key_bindings = key_bindings
  end
end

# Define a new bindings instance for the game
bindings = ActionBindings.new(:mouseLook,
  :move_left   => :A.key, 
  :move_right  => :D.key, 
  :crouch     => :S.key, 
  :jump       => :W.key)