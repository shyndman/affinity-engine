
class Entity    
  
  #
  # The entity's current state. Each entity in the game world is only ever in one state. Its state reflects
  # the action it wants to perform.
  #
  attr_reader :current_state
  
  #
  # Initializes the entity
  #
  def initialize(*args)
    @is_initialized = true
    @in_state_code = false
  end
  
  #
  # Returns true if the entity has been initialized. This is used by various systems
  # to ensure that the entity has been properly created.
  #
  def is_initialized?; @is_initialized; end
  
  #
  #
  #
  def goto_state state_name
    
  end
  
  #
  # Used to define state methods on an entity. A state method is executed as an Entity
  # enters a new state. States support 
  #
  def self.state(*args, &block)
    setup_state_support()
    
    #
    # Get the symbol
    # 
    symbol = args.first
    options = args.length > 1 ? args[1] : nil
    
    #
    # Raise an error if we already have a state named symbol defined
    #
    if @state_table.has_key? symbol
      raise NameError.new("A state named #{symbol} has already been defined on #{self}", symbol)
    end
    
    #
    # Add the state to the state table
    #
    @state_table[symbol] = StateInfo.new symbol, options, &block
  end

  private
  
  #
  # Does initial setup for state support, when the first entity state is defined in the application
  #
  def self.setup_state_support
    return if self.instance_variable_defined? :@has_state_support
    
    puts "setting up state support for #{self}"
    @has_state_support = true
    @state_table = {}
  end
end

#
# Contains state information, like the name of the state, options related to the state, and the state's 
# implementation
#
class State
  attr_reader :symbol, :options, :block
  
  #
  # Initializes the state with all necessary information
  #
  def initialize(symbol, options, &block)
    @symbol, @options, @block = symbol,options,block
  end
  
  #
  # Returns a string representation of the state
  #
  def to_s
    "state: #{@symbol} (options=#{options})"
  end
end

#
# Basic subclass for testing purposes
#
class SubEntity < Entity  
  state :begin do ||
    puts "begin state"
  end
  
  state :another do ||
    
  end
end

e = SubEntity.new
