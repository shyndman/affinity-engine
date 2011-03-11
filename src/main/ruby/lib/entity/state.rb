#
# Contains state information, like the name of the state, options related to the state, and the state's 
# implementation
#
class State
  attr_reader :symbol, :entity_class, :options
  attr_accessor :method
  
  #
  # Initializes the state with all necessary information
  #
  def initialize(symbol, entity_class, options, method)
    @symbol, @entity_class, @method = symbol, entity_class, method
    @options = {
      :ignores => []
    }.merge(options || {})
    @options[:ignores] = [@options[:ignores]] unless @options[:ignores].respond_to?(:include?)
  end
  
  #
  # Returns true if this state ignores invocations of the function specified.
  #
  def ignores_function? symbol
    @options[:ignores].include? symbol
  end
  
  #
  # Runs the state in the context of the provided entity.
  #
  def run entity
    return if @block.nil?
    
    @method.call
  end
  
  #
  # Returns a string representation of the state
  #
  def to_s
    "State(symbol=#{@symbol}, options=#{options}, method=#{method})"
  end
end

#
# A state table is used by entity classes to store available states, as well as special states (like :initial)
#
class StateTable < Hash

  #
  # Stores the initial state an entity will be in when it is created.
  #
  attr_accessor :initial_state

  def initialize
    super
  end
end