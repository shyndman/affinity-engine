require 'entity/state'
require 'entity/threading'
require 'util/reflect'
#
# Kernel additions
#
module Kernel
  def dbg *args
    puts *args
  end
end

#
# Defines a game entity
#
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
    @is_state_change_queued = false
    
    #
    # Call various initialization methods
    #
    initialize_threading
    
    #
    # Set the initial state
    #
    state = get_initial_state
    _goto_state(state, false)
  end
  
  #
  # Returns true if the entity has been initialized. This is used by various systems
  # to ensure that the entity has been properly created.
  #
  def is_initialized?; @is_initialized; end
  
  #
  # ==== ADDING AND CALLING METHODS
  #
  
  private
  
  GENERAL_METHOD_PREFIX = "__aff_method_"
  INSTANCE_METHOD_PREFIX = "#{GENERAL_METHOD_PREFIX}instance_"
  STATE_METHOD_PREFIX = "#{GENERAL_METHOD_PREFIX}state_"
  
  #
  # Unwraps the method if it is wrapped
  #
  def self.unwrap_method name
    return if not @wrapped_instance_methods.has_key? name
    
    dbg "unwrapping method #{self}.#{name}"
    remove_method name
    alias_method name, instance_method_name(name)
  end
  
  #
  # Calls all registered method_added handlers
  #
  def self.method_added name
    wrap_method name
  end

  #
  # All methods defined in subclasses are prefixed so their calls can be intercepted
  # and ignored if stated in the entity's current state
  #  
  def self.wrap_method name
    name = name.intern unless name.kind_of? Symbol
    
    #
    # Have we already wrapped?
    #
    return if self == Entity or name =~ /^#{GENERAL_METHOD_PREFIX}/o or @wrapped_instance_methods.has_key? name
    
    #
    # Alias the method and remove its original. The wrapper method will handle the rest
    #
    dbg("aliasing instance method #{self}.#{name}")

    #
    # Get information about the method
    #
    info = Introspector.get_method_info self, name
    
    #
    # Alias the method, then remove it
    #
    alias_method instance_method_name(name).intern, name
    remove_method name
    
    #
    # Add the name to a map of all the functions we have wrapped on this class
    #
    @wrapped_instance_methods[name] = true
    
    #
    # Unwrap the superclass implementation of the instance method if it exists
    #
    self.superclass.__send__(:unwrap_method, name) if self.superclass.methods.include? :unwrap_method
    
    #
    # Add a stub method that performs checks against the current state
    #
    define_method(name) do |*args, &block|
      run_wrapped_instance_method name, *args, &block
    end
  end
  
  #
  # Used to filter calls to specific methods while the entity exists within a state where 
  #
  def run_wrapped_instance_method method, *args, &block
    #
    # Make sure we have the instance method. If we don't, simulate the default behaviour of raising an
    # exception
    #
    if not has_instance_method(method)
      raise NoMethodError.new("undefined method ‘#{method}’ for #{self.inspect}", method) 
    end
    
    #
    # Get the state, and check whether this function call is ignored
    #
    if not @current_state.nil? and @current_state.ignores_function? method
      dbg "ignoring call to #{method}"
      return nil
    end
    
    #
    # Call the method
    #
    dbg("calling #{method}")
    __send__ instance_method_name(method), *args, &block
  end
  
  #
  # Returns the true name of the specified instance method.
  #
  def self.instance_method_name method_name
    "#{INSTANCE_METHOD_PREFIX}#{method_name}"
  end
  def instance_method_name method_name; self.class.instance_method_name method_name; end
  
  #
  # Returns true if this object has a (wrapped) instance method who's external name is method_name
  #
  def has_instance_method method_name
    method_name = method_name.intern unless method_name.kind_of? Symbol
    self.respond_to?(instance_method_name(method_name), true)
  end
  
  #
  # ==== SUBCLASS SETUP
  #
  
  #
  # Performs necessary decoration on all classes that subclass this one.
  #
  def self.inherited subclass
    class_initialize subclass
  end
  
  #
  # Initializes everything necessary for this class to function
  #
  def self.class_initialize cls = self
    dbg "class init: #{cls}"
    cls.instance_variable_set :@wrapped_instance_methods, {}
    cls.instance_eval { setup_state_support() }
  end
  
  #
  # ==== STATE RELATED CODE
  #
  
  public
  
  #
  # Returns true if a state change has been queued but not yet executed.
  #
  def is_state_change_queued?
    @is_state_change_queued
  end
  
  #
  # Changes the entity's state. +state+ can be a +State+ object or the name of a state that is defined
  # on this entity's class, or one of its superclasses.
  #
  def goto_state state
    _goto_state state
  end
  
  private
  
  #
  # The internal implementation of goto_state, with optional argument checking
  #
  def _goto_state state, raise_errors = true
    if not state.kind_of? State
      #
      # Do a null check on the state
      #
      if state.nil?
        if raise_errors
          raise ArgumentError.new "state argument cannot be nil"
        end
      
        return
      end
    
      #
      # Attempt to get the named state
      #
      name = state.intern unless state.kind_of? Symbol
      state = get_state state
    else
      raise ArgumentError.new "state must belong to this entity class, or one of its superclasses" \
        unless self.kind_of? state.entity_class
    end

    #
    # Raise an error if no state was found
    #
    if state.nil? 
      if raise_errors 
        raise ArgumentError.new "no state named #{name} found" 
      end
      
      return
    end
    
    #
    # Set the current state
    #
    dbg "setting state to #{state}"
    @current_state = state
  end
  
  public
  
  #
  # Gets the State object with the specified name, or nil of none exists. This method will search superclasses if
  # the state isn't found defined on this class. A superclass to start the search from can
  # optionally be provided.
  #
  def get_state state_name, superclass = self.class
    superclass.get_state state_name
  end
  
  #
  # Used to define the initial state the entity should be in. The syntax is identical to state.
  #
  # This method can only be called once per class definition.
  #
  def self.initial state, &block
    raise Exception.new("cannot define initial state #{state.symbol} - an initial state #{@state_table.initial_state.symbol} already exists") \
      unless @state_table.initial_state.nil?
    
    dbg "setting #{state.symbol} as the initial state"
    @state_table.initial_state = state
    
    #
    # Add the state method if necessary
    #
    method = define_state_method state.symbol, state.options, &block
    state.method = method
  end
  
  #
  # Used to define state methods on an entity. A state method is executed as an Entity
  # enters a new state.
  #
  # +options+ is an optional hash of options. The following example lists
  # all options and their default values.
  # 
  # The options have the following meanings:
  #
  # :ignores   the symbol or array of symbols representing the methods that will be ignored
  #            while the entity occupies this state
  # =Example
  #
  #   class Light
  #
  #     state :on, :ignore => :turn_on, do
  #       # ... do whatever
  #     end
  #
  #     state :off, :ignore => [:turn_off, :turn_off_slowly], do
  #       # ... do whatever
  #     end
  #
  #     ...
  #   end
  #
  # Can be used with +initial+ to define an initial state.
  #
  def self.state(symbol, options = nil, *args, &block)        
    #
    # Raise an error if we already have a state named symbol defined
    #
    if @state_table.has_key? symbol
      raise NameError.new("a state named #{symbol} has already been defined on #{self}", symbol)
    end
    
    dbg "adding #{symbol} state"
    
    #
    # Define the state method
    #
    method = define_state_method symbol, options, &block
    
    #
    # Add the state to the state table
    #
    @state_table[symbol] = State.new symbol, self, options, method
  end  
  
  #
  #
  #
  def self.define_state_method state_name, options, &block
    method_name = state_method_name state_name    
    method = block.nil? ? nil : define_method(method_name, block)
  end
  
  #
  # Returns the state method name for the given state symbol
  #
  def self.state_method_name symbol
    return "#{STATE_METHOD_PREFIX}#{symbol}".intern
  end
  
  private  
  
  #
  # Gets the state named
  #
  def self.get_state state_name
    if @state_table.has_key? state_name
      return @state_table[state_name]
    elsif superclass.method.include? :get_state
      return superclass.__send__ :get_state, state_name
    end
    
    nil
  end
  
  #
  # Returns the entity's initial state.
  #
  def self.get_initial_state
    if not @state_table.initial_state.nil?
      return @state_table.initial_state
    elsif superclass.methods.include? :get_initial_state
      return superclass.__send__ :get_initial_state
    end
  end
  
  #
  # Instance method helper
  #
  def get_initial_state; self.class.get_initial_state; end
  
  #
  # Does initial setup for state support, when the first entity state is defined in the application
  #
  def self.setup_state_support
    return if self.instance_variable_defined? :@has_state_support
    
    puts "setting up state support for #{self}"
    @has_state_support = true
    @state_table = StateTable.new
  end
  
  #
  # ===== STATE INTERNAL METHODS
  #
  
  protected
  
  #
  # 
  #
  def sleep duration_in_seconds
    sleep_fiber = Fiber.new do
      dbg "sleeping for #{duration_in_seconds} seconds"
      Kernel.sleep duration_in_seconds
      dbg "done sleeping"
    end
    sleep_fiber.resume
  end
  
  #
  # Initialize the class
  #
  class_initialize()
end

#
# Tests
#
if __FILE__ == $0
  #
  # Basic subclass for testing purposes
  #
  class A < Entity  
    def snaz
      puts "A snaz called"
    end
    
    initial state :begin do
      puts "begin state entered"
      sleep 1
    end
  
    state :another do
    
    end
  end 
  
  class B < A
    def snaz
      puts "B snaz called"
      super
    end
    
    initial state :begin do
      puts "subclass begin state entered"
      super()
    end
  end
  
  b = B.new
  
  b.__aff_method_state_begin
end