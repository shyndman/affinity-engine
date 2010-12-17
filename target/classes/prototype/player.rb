module Prototype 
  #
  # Define the action bindings for the gae
  #
  Affinity::ActionBindings.new({
    :move_left => Affinity::Input::Key::LEFT,
    :move_right => Affinity::Input::Key::RIGHT,
    :jump => Affinity::Input::Key::UP,
    :crouch => Affinity::Input::Key::DOWN,
    :spin_left => Affinity::Input::Key::A,
    :spin_right => Affinity::Input::Key::D
  }, :context => "player")
  
  #
  # Create an entity class for representing a player, then create an instance
  #
  class PlayerEntity < Affinity::Entity
  
    on_event :move_left do |info|
      if info.value == 0
        @sprite.x.behavior = nil
      else
        @sprite.x >> -100
      end
    end
  
    on_event :move_right do |info|
      if info.value == 0
        @sprite.x.behavior = nil
      else
        @sprite.x >> 100
      end
    end 
  
    on_event :jump do |info|
      if info.value == 0
        @sprite.y.behavior = nil
      else
        @sprite.y >> 100
      end
    end
  
    on_event :crouch, :action_speed => 10 do |info|
      @sprite.y - (info.time * 200)
      @sprite.y.to 100.over 2.seconds
      event_wait_for @sprite.y
    end
    
    on_event :spin_left do |info|
      if info.value == 0
        @sprite.angle.behavior = nil
      else
        @sprite.angle >> 100
      end
    end
    
    on_event :spin_right do |info|
      if info.value == 0
        @sprite.angle.behavior = nil
      else
        @sprite.angle >> -100
      end
    end
  end

  #
  # Create a player
  #
  $player = PlayerEntity.new :name => "player" do
    # load a texture
    t = tex "virus.png", :classpath => true

    
    # create a graphic
    g = graphic(t, :x => 150, :y => 100)
  end
  
  #
  # Tack the player's node onto the root node
  #
  puts "adding player"
  puts engine.env.rootNode
  engine.env.rootNode.attachChild $player.sprite
end