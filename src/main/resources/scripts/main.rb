# Startup. Let's set up our paths.
require 'singleton'

# XXX This is a bad test. Consider looking for some env flag.
if Dir['.'].include? "pom.xml"
  $:.concat ['src/main/resources/scripts']
  require 'java'
end

#
# The Game module
#
class ScriptEngine
  include Singleton
  
  #
  # Holds event handlers
  #
  attr_reader :event_handlers
  
  #
  # Holds configuration information
  #
  module Config
  end
  
  def initialize
    @event_handlers = []
  end

  #
  # Starting up
  #
  def start
    load_libs()
    load_game()
    self
  end
  
  #
  # Load in all necessary librarys
  #
  def load_libs
    loadQueue = []
    Pathname.glob './lib/*.rb' do |script|
      require script.to_s
      scriptfolder = Pathname.new(script.to_s.gsub('.rb', ''))
      next if not scriptfolder.directory?

      loadQueue += scriptfolder.children(true).find_all {|file| file.to_s[-3..-1] == '.rb'}
    end
    
    # load the children
    loadQueue.each {|file| require file.to_s }
  end
  
  #
  # Start loading in game data
  #
  def load_game
    require './game/setup.rb'
  end
  
  #
  # Register handler
  #
  def register_handler(handler_proc)
    @event_handlers.push handler_proc 
  end
end

ScriptEngine.instance.start