import "ca.scotthyndman.game.engine.animation.Timeline"

module Affinity
  
  #
  # Mixed into sprites to provide animation capabilities
  #
  module Animation
    #
    # Animates a sprite
    #
    def animate options, &block
      
    end
    
    #
    # Builds a timeline animation.
    #
    def schedule *args
      fill_timeline Timeline.new(), args
    end
    
    #
    # Builds a timeline animation.
    #
    def schedule_in delay, *args
      fill_timeline Timeline.new(delay), args
    end
    
    #
    # Performs an action at a specific time.
    #
    def in options, &block
      
    end
    
    #
    # A timeline event that adds a sprite to a group
    #
    def add_sprite options, &block

    end
    
    #
    # A timeline event that removes a sprite from a group
    #
    def remove_sprite options, &block
      
    end
    
    #
    # A timeline event that plays a sound
    #
    def play_sound options
      
    end
    
    private
    
    #
    # This method is responsible for filling a timeline with events
    #
    def fill_timeline timeline, events
      
    end
    
    #
    # Contains duration, target value and easing information for a tween
    #
    class TweenInfo
      attr_accessor :to, :duration, :easing, :delay
      
      def initialize to, ms, easing = :ease_none, delay = 0
        raise ArgumentError.new("ease_type must be a symbol") unless easing.kind_of?(Symbol)
        @to, @duration, @easing, @delay = to, ms, easing.ease, delay
      end
    end
  end
end