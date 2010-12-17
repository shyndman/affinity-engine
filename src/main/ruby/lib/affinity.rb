$basedir = "src/main/ruby/lib/"

#
# Require away
#
require 'pp'
require 'singleton'
load 'util/util.rb'
begin; require 'java'; rescue Exception; puts "WARNING! TESTING MODE! WARNING!\n\n"; testing = true; require 'debug/debug'; $:.push "../prototype" end

#
# Import java classes
#
import "ca.scotthyndman.game.engine.scripting.ScriptActionHandler"
import "ca.scotthyndman.game.engine.scripting.ScriptInputActionHandler"

#
# Core module definition
#
module Affinity
  #
  # Provides methods for Ruby code to build objects related to the game
  #
  module Builder
    
    #
    # Includes
    #
    def self.load_includes    
      Util.load_dir 'builder', :type => "Builder"
    end
  end
  
  #
  # The entity class is the base class for game logic
  #
  class Entity
    include Affinity::Builder
    
    attr_reader :sprite
    
    def new(*args, &block)
      puts "does this get called?"
      super
    
      puts block.inspect.to_s
      if not defined? @is_inited
        raise RuntimeError.new("Entity.initialize was never called. Make sure your subclass calls super.")
      end
    end
    
    #
    # Overwritten to set an is_inited flag, so we can throw an error if someone doesn't call super. 
    #
    def initialize(options = {}, &block)
      @is_inited = true
      @options = {}.merge(options)
      
      # Perform any init operations
      if self.class.methods.include? "init_methods" and not self.class.init_methods.nil?
        self.class.init_methods.each do |name, method|
          method.bind(self).call
        end
      end
      
      if block
        @sprite = instance_eval(&block) 
      end
    end
    
    #
    # Includes
    #
    def self.load_includes
      Util.load_dir 'entity', :first => 'core.rb', :type => "Entity"
    end
  end
  
  #
  # The bridge class between Java and Ruby
  #
  class ScriptEngine
    include Singleton
    
    def initialize
      @action_handlers = []
      $engine_loaded = true
    end
    
    #
    # Gets or sets the environment value
    #
    def env value = nil
      @env = value || @env
    end
    
    #
    # Gets or sets the input manager
    #
    def input_system value = nil
      @input_system = value || @input_system
    end
    
    def event_manager value = nil
      @event_manager = value || @event_manager
    end
    
    #
    # Gets or sets the input manager
    #
    def specials value = nil
      value.nil? ? @specials : @specials = value
    end
    
    #
    # Invoked when the script engine should start doing its thing
    #
    def ready game_script
      @game_script = game_script
      old_requires = $".clone
      load (game_script =~ /\.rb$/ ? game_script : game_script + '.rb')
      @require_diffs = $" - old_requires
    end
    
    #
    # Returns the engine's action handlers
    #
    def action_handlers
      @action_handlers
    end
    
    #
    # Adds an action handler to the script engine
    #
    def add_action_handler info
      @action_handlers.push info
      # puts "adding action handler #{info.inspect}"
      if @input_system and @input_system.has_action?(info[:action].to_s)
        handler = info.kind_of?(ScriptActionHandler) ? info : ScriptActionHandler.new(info)
        @input_system.add_action_handler handler.action_name, handler
      elsif @event_manager
        handler = info.kind_of?(ScriptActionHandler) ? info : ScriptActionHandler.new(info)
        @event_manager.add_action_handler handler.action_name, handler
      else
        puts "adding fake action handler"
        return
      end
      
      handler
    end
    
    #
    # Registers a collection of action bindings with the engine
    #
    def register_action_bindings bindings
      bindings.input_bindings.each do |action, id|
        # puts "adding action binding #{action} => #{id}"
        if @input_system
          @input_system.addActionBinding id.identifier, action.to_s
        else
          puts "adding fake action binding"
        end
      end
    end
    
    #
    # Removes all action handlers from the input manager
    #
    def remove_handlers
      @input_system.clear_action_handlers
      @input_system.clear_action_bindings
      @event_manager.clear_action_handlers
    end
    
    def remove_modules
      removable = ["Util", "ActionBindings", "Input", "Animation"]
      removable.each do |m|
        Affinity.send :remove_const, m.intern
      end
    end
    
    def reload_scripts
      load 'util/util.rb'
      Util.load_dir 'animation', :type => "Animation", :first => ["easing.rb", "core.rb"]
      Util.load_dir 'scene', :type => "Scene"
      Affinity::Builder.load_includes
      Affinity::Entity.load_includes
    end
    
    def detach_children
      @env.rootNode.detachAllChildren
    end
    
    def reload_game
      if defined? @require_diffs
        @require_diffs.each do |file|
          $".delete file
        end
      end
      
      ready @game_script
    end
  
    def reset
      detach_children
      remove_handlers
      remove_modules
      reload_scripts
      reload_game
    end
  end
  
  ScriptEngine.instance.reload_scripts
end

#
# Shortcut to get to the engine
#
def engine
  Affinity::ScriptEngine.instance
end

#
# Shortcut to the environment object
#
def env
  engine.env
end

Affinity::ScriptEngine.instance