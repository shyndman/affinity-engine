module Prototype 
  
  #
  # Create an entity class for representing the cursor
  #
  class Cursor < Affinity::Entity
    on_event :tick do |info|
#      puts "#{env.input_state.mouse_x}, #{env.input_state.mouse_y}"
      @sprite.x.set env.input_state.mouse_x
      @sprite.y.set env.input_state.mouse_y
    end
  end

  #
  # Create a cursor
  #
  $cursor = Cursor.new :name => "cursor" do
    
    # build a node
    n = node("cursor", :x => env.input_state.mouse_x, :y => env.input_state.mouse_y)
    
    # load a texture
    t = tex "flower_pointer.png", :classpath => true
    
    # polyline border
    w = t.image.width
    h = t.image.height
#    pl = polyline [-w - 3, 15, 33, -30, 15, 3, 0, h, -w - 3, 22, 150, -200, 94, 71], 
#      :name => "borderline", :line_width => 10, :color => ColorRGBA.new(0.3, 1, 0.2, 1)
#    puts "new size: #{n.content_node.attach_child(pl)}" 
#    puts pl.inspect
    
    # create a graphic
    g = graphic(t, :x => 0, :y => 0)
    n.add g   
    n
  end
  
  #
  # Spin it
  #
  #$cursor.sprite.angle >> 60
  
  #
  # Tack the cursor's node onto the root node
  #
  engine.env.rootNode.attach_child $cursor.sprite
end