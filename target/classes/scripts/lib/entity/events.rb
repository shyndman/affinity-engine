#
# Extensions to the Entity class to provide an event DSL
#
class Entity
    
  def selfbind
    binding # has this in it
  end
    
  # 
  # Returns the metaclass for this class.
  #  
  def self.metaclass; class << self; self; end; end
    
  # Responsible for building the event handler. Tells the ScriptEngine about the registration.
  def self.on_event(symbol, &block)
    new_method = nil
    # add a method to the metaclass (which is instance specific)
    class_eval do
      new_method = define_method("on_#{symbol.to_s}".intern, &block)
    end
    
    ScriptEngine.instance.register_handler new_method
  end
end

$logger.info("Entity decorated with events") if defined? $logger