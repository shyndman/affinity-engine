import "ca.scotthyndman.game.engine.animation.Easing"

#
# Maps symbols onto eases
#
class Symbol
  def ease
    ease_symbol = self.to_s.upcase.intern
    ease_symbol = :NONE unless Easing.const_defined? ease_symbol
    Easing.const_get ease_symbol
  end
end

#
# Adds easing constants to the module
#
module Affinity
  module Animation
    #
    # Defines all the eases
    #
    module Ease
      NONE = :none.ease
      REGULAR_IN = :regular_in.ease
      REGULAR_OUT = :regular_out.ease
      REGULAR_IN_OUT = :regular_in_out.ease
      STRONG_IN = :strong_in.ease
      STRONG_OUT = :strong_out.ease
      STRONG_IN_OUT = :strong_in_out.ease
      BACK_IN = :back_in.ease
      BACK_OUT = :back_out.ease
      BACK_IN_OUT = :back_in_out.ease
      ELASTIC_IN = :elastic_in.ease
      ELASTIC_OUT = :elastic_out.ease
      ELASTIC_IN_OUT = :elastic_in_out.ease
    end
  end
end

