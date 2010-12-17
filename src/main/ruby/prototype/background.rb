import "com.jme.scene.state.LightState"
import "com.jme.scene.Spatial"
import "com.jme.renderer.Renderer"
import "com.jme.system.DisplaySystem"
module Prototype 
  
  #
  # Create an entity class for representing the cursor
  #
  class Background < Affinity::Entity
  end

  #
  # Create a cursor
  #
  $background = Background.new :name => "bg" do
        
    # load a texture
    t = tex "flower_pointer.png", :classpath => true
    
    w = t.image.width
    h = t.image.height    
    
    # create a graphic
    g = graphic(t, :x => 0, :y => 0)
    g.scaleX.set(800 / w)
    g.scaleY.set(600 / h)
    g.x.set 400
    g.y.set 300
    g
  end
  
  #
  # Tack the cursor's node onto the root node
  #
  puts $background
  engine.env.rootNode.attach_child $background.sprite
end