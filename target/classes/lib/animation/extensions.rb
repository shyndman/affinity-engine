import "ca.scotthyndman.game.engine.animation.Property"

#
# This file extends native types for animation goodness
#

#
# Extended to provide tween infos
#
class Object
  #
  # Returns a TweenInfo containing a target value and the number of milliseconds the 
  # tween should take place.
  #
  # options can contain an :with_ease key which specifies which ease to use, as a Symbol
  #
  def over ms, options = {}
    options = {:with_ease => :ease_none, :delay_by => 0}.merge(options)
    return Affinity::Animation::TweenInfo.new(self, ms, options[:with_ease], options[:delay_by])
  end
end

#
# Extended to provide unit changes
#
class Numeric
  #
  # Returns the number of milliseconds if this number represented a number of seconds
  #
  def seconds
    return self * 1000
  end
end

#
# Makes it easier to trace properties
#
class Property
  #
  # The to method is used as a sort of DSL for tweening. A tween may look as follows:
  #
  # => property.to 55.over 3.seconds, :with_ease => :strong_in, :delay_by => 3.seconds
  #
  def to tween_info
    #
    # ATTENTION!! animateTo isn't actually available on the property. we're making the assumption that it will be there.
    #
    animateTo(tween_info.to, tween_info.duration, tween_info.easing, tween_info.delay)
  end
  
  def >>(step)
    #
    # ATTENTION!! constant isn't actually available on the property. we're making the assumption that it will be there.
    #
    constant(step)
  end
end