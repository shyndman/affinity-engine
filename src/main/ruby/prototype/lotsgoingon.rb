
module LotsGoingOn
  #
  # Create an entity class for representing the cursor
  #
  class Cursor < Affinity::Entity
    on_event :tick do |info|
      @sprite.x.set env.input_state.mouse_x
      @sprite.y.set env.input_state.mouse_y
    end
  end
  
  #
  # The spinning hamster class
  #
  class Hamster < Affinity::Entity
    on_event :tick do |info|
      ease = :regular_in_out
      time = (rand(6) + 2)
      delay = rand(3)
      sprite.x.to rand(800).over time.seconds, :with_ease => ease, :delay_by => delay
      sprite.y.to rand(600).over time.seconds, :with_ease => ease, :delay_by => delay
      sprite.angle.to rand(720).over time.seconds, :with_ease => ease, :delay_by => delay
      
      #
      # Don't call tick again until we're done tweening
      #
      event_wait_for sprite.x.behavior
    end
  end
  
  #
  # Create 501 beasts! (attached at scene graph root)
  #
  (0...500).each do |i|
    h = Hamster.new :name => "ham#{i}" do
      t = tex "hamster.png", :classpath => true
      g = graphic(t, :x => rand(800), :y => rand(300))
    end
    
    env.root_node.attach_child h.sprite
  end
  
  #
  # Create a cursor
  #
  $cursor = Cursor.new :name => "cursor" do
    
    # build a node
    n = node "cursor", :x => env.input_state.mouse_x, :y => env.input_state.mouse_y
    
    # load a texture
    t = tex "flower_pointer.png", :classpath => true
    
    # create a graphic
    g = graphic t, :x => 0, :y => 0
  end
  
  #
  # Spin it
  #
  $cursor.sprite.angle >> 60
  
  #
  # Tack the cursor's node onto the root node
  #
  engine.env.rootNode.attach_child $cursor.sprite
end