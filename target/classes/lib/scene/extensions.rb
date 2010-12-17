import 'ca.scotthyndman.game.engine.scene.Positioned'
import 'ca.scotthyndman.game.engine.animation.Property'
import 'ca.scotthyndman.game.engine.animation.Int'
import 'ca.scotthyndman.game.engine.animation.Real'
import 'com.jme.renderer.ColorRGBA'

class Positioned
  include Affinity::Animation
  
  def initialize *args
    super
    
    @x, @y, @scale_x, @scale_y, @center_x, @center_y, @angle, @alpha, @visible = x, y, scaleX, scaleY, centerX, centerY, angle, alpha, visible
  end
end

#
# Makes it easier to trace properties
#
class Property
  def initialize *args
    super
    
    @value = value
  end
  
  def to_s
    toString
  end
  
  def inspect
    to_s
  end
end

#
# Operators for property modification
#
module NumericOperators
  def +(oth)
    if (oth.kind_of? Numeric)
      set(get + oth)
    end
  end
  
  def -(oth)
    if (oth.kind_of? Numeric)
      set(get - oth)
    end
  end
  
  def *(oth)
    if (oth.kind_of? Numeric)
      set(get * oth)
    end
  end
  
  def /(oth)
    if (oth.kind_of? Numeric)
      set(get / oth)
    end
  end
  
  def %(oth)
    if (oth.kind_of? Numeric)
      set(get % oth)
    end
  end
end

#
# Add some operators
#
class Real
  include NumericOperators
end

#
# Add some operators
#
class Int
  include NumericOperators
end

#
# Add colors and operators
#
class Fixnum
  include NumericOperators
  
  def to_rgba
    c = ColorRGBA.new
    c.fromIntRGBA self
    c
  end
  
  def to_argb
    c = ColorRGBA.new
    c.fromIntARGB self
    c
  end
end