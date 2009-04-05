#
# Stores behaviors. This is the bean exposed to the game.
#
class BehaviorRepository
  def initialize
    @@behaviors = []
  end
  
  def behaviors
    @@behaviors
  end
  
  def BehaviorRepository.add(behav)
    behav = Behavior.new behav
    @@behaviors.concat [behav]
  end
end

#
# Describes the behavior of all nodes that match the specified selector.
#
def describe(selector, &block)
  BehaviorBuilder.new selector, &block
end

#
# The BehaviorBuilder is responsible for associating selectors with arbitrary behavior blocks. A shortcut for the creation
# of a builder, is describe().
#
class BehaviorBuilder  
  
  attr_accessor :selector
  
  #
  # Initializes a new BehaviorBuilder
  #
  def initialize(selector, &block)
    self.selector = selector
    @event_blocks = block.nil? ? {} : {:init => block}
    BehaviorRepository.add self
    self
  end
  
  #
  # Allows the definition of a handler for any arbitrary event. The block will be called with event specific information.
  #
  def method_missing(sym, &block)
    @event_blocks[sym] = block
    self
  end
  
  #
  # Handles an event. info should contain at least a node entry, as well as any event-specific information.
  #
  def handle(event, info)
    @event_blocks[event].call info if @event_blocks.has_key? event
  end
end

#
# Create a repository
#
BehaviorRepository.new