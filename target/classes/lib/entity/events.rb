if not defined? import
  def import file; file; end
end

import "ca.scotthyndman.game.engine.scripting.ScriptActionHandler"
import "net.java.games.input.Component"

module Affinity
  #
  # ActionBindings are created to create associations between input device actions and named game "actions".
  # ActionBindings are registered with the system immediately upon creation.
  #
  class ActionBindings
    attr_reader :input_bindings
    
    def initialize input_bindings, options = {}
      @options = {:context => :default}.merge(options)
      @input_bindings = input_bindings
      
      Affinity::ScriptEngine.instance.register_action_bindings self
    end
  end
  
  #
  # Extended to support event handling DSL
  #
  class Entity
    
    def self.events() 
      return @events
    end
    
    def self.init_methods()
      return @init_methods
    end
    
    # If the result of this method is returned from an event handler, the event handler will not be called
    # again until finishable is finished
    def event_wait_for finishable
      ScriptActionHandler::HandlerResult::waitFor finishable
    end
    
    # If the result of this method is returned from an event handler, the event handler will not be called
    # again until the delay has expired
    def event_wait_delay seconds
      ScriptActionHandler::HandlerResult::waitDelay seconds
    end
    
    # If the result of this method is returned from an event handler, the event handler will not be called
    # again.
    def event_terminate 
      ScriptActionHandler::HandlerResult::terminate
    end
    
    #
    # Toggles whether this entity receives the tick event. This is only meaningful if a :tick actionhandler 
    # is defined.
    #
    def tick flag
      return unless self.class.method_defined? :on_tick
      @is_ticking = self.class.method_defined?(:old_on_tick) unless instance_variable_defined?(:@is_ticking)
      
      return if @is_ticking == flag 
        
      @is_ticking = flag
      if flag
        alias_method :on_tick, :old_on_tick
        engine.add_action_handler @action_handlers[:tick]
      else
        alias_method :old_on_tick, :on_tick
        def self.tick; event_terminate; end
      end
    end
    
    # Responsible for building the event handler. Tells the ScriptEngine about the registration.
    def self.on_event(*args, &block)
      new_method = nil
      
      # get the symbol
      symbol = args.first
      raise ArgumentError.new("on_event requires a symbol") if symbol.nil?
      
      # if we have a second argument, it will be an options hash
      options = args.length > 1 ? args[1] : nil
      
      # create an information object
      info = {:action => symbol, :options => options || {:action_speed => 0}}
      
      # add a method to the metaclass (which is instance specific)
      @events ||= {}
      @events[symbol] = info
      
      # init events
      @init_methods ||= {}
      
      # add a special initialization method
      class_eval do
        handler_name = info[:handler_name] = "on_#{symbol.to_s}"
        new_method = define_method(handler_name.intern, &block)
        
        # Define an init method if we have to
        is_init_defined = method_defined? :event_init
        return if is_init_defined

        define_method :event_init do 
          self.class.events.each do |action, info|
            @action_handlers = {}
            @action_handlers[info[:action]] = engine.add_action_handler info.merge(:object => self)
          end
        end
        init_method = instance_method :event_init
        @init_methods[:events] = init_method
      end
    end
  end
  
  #
  # The Input module is a container module for modules that identify input component types, like keyboard keys, mouse
  # buttons, controllers, etc.
  #
  module Input
    #
    # An axis is a single button, slider, or dial, which has a single range.  An
    # axis can hold information for motion (linear or rotational), velocity,
    # force, or acceleration.
    #
    module Axis
      #
      # An axis for specifying vertical data.
      #
      X = "x"

      #
      # An axis for specifying horizontal data.
      #
      Y = "y"

      #
      # An axis for specifying third dimensional up/down
      # data, or linear data in any direction that is
      # neither horizontal nor vertical.
      #
      Z = "z"

      #
      # An axis for specifying left-right rotational data.
      #
      RX = "rx"

      #
      # An axis for specifying forward-back rotational data.
      #
      RY = "ry"

      #
      # An axis for specifying up-down rotational data
      # (rudder control).
      #
      RZ = "rz"

      #
      # An axis for a slider or mouse wheel.
      #
      SLIDER = "slider"

      #
      # An axis for slider or mouse wheel acceleration data.
      #
      SLIDER_ACCELERATION = "slider-acceleration"

      #
      # An axis for slider force data.
      #
      SLIDER_FORCE = "slider-force"

      #
      # An axis for slider or mouse wheel velocity data.
      #
      SLIDER_VELOCITY = "slider-velocity"

      #
      # An axis for specifying vertical acceleration data.
      #
      X_ACCELERATION = "x-acceleration"

      #
      # An axis for specifying vertical force data.
      #
      X_FORCE = "x-force"

      #
      # An axis for specifying vertical velocity data.
      #
      X_VELOCITY = "x-velocity"

      #
      # An axis for specifying horizontal acceleration data.
      #
      Y_ACCELERATION = "y-acceleration"

      #
      # An axis for specifying horizontal force data.
      #
      Y_FORCE = "y-force"

      #
      # An axis for specifying horizontal velocity data.
      #
      Y_VELOCITY = "y-velocity"

      #
      # An axis for specifying third dimensional up/down acceleration data.
      #
      Z_ACCELERATION = "z-acceleration"

      #
      # An axis for specifying third dimensional up/down force data.
      #
      Z_FORCE = "z-force"

      #
      # An axis for specifying third dimensional up/down velocity data.
      #
      Z_VELOCITY = "z-velocity"

      #
      # An axis for specifying left-right angular acceleration data.
      #
      RX_ACCELERATION = "rx-acceleration"

      #
      # An axis for specifying left-right angular force (torque) data.
      #
      RX_FORCE = "rx-force"

      #
      # An axis for specifying left-right angular velocity data.
      #
      RX_VELOCITY = "rx-velocity"

      #
      # An axis for specifying forward-back angular acceleration data.
      #
      RY_ACCELERATION = "ry-acceleration"

      #
      # An axis for specifying forward-back angular force (torque) data.
      #
      RY_FORCE = "ry-force"

      #
      # An axis for specifying forward-back angular velocity data.
      #
      RY_VELOCITY = "ry-velocity"

      #
      # An axis for specifying up-down angular acceleration data.
      #
      RZ_ACCELERATION = "rz-acceleration"

      #
      # An axis for specifying up-down angular force (torque) data.
      #
      RZ_FORCE = "rz-force"
      #
      #
      # An axis for specifying up-down angular velocity data.
      #
      RZ_VELOCITY = "rz-velocity"

      #
      # An axis for a point-of-view control.
      #
      POV = "pov"

      #
      # An unknown axis.
      #
      UNKNOWN = "unknown"
        
      #
      # A hash storing all the axis names
      #
      NAMES = {}
      
      #
      # Component identifiers
      #
      IDENTIFIERS = {}

    	#
    	# Initializes the NAMES hash. Runs as the module is being initialized.
    	#
    	self.constants.each do |name| 
    	  next if name == "NAMES" or name == "IDENTIFIERS"

    	  val = Axis.const_get(name.intern);
    	  def val.identifier 
    	    Axis.identifier_of self
  	    end
    	  
    	  NAMES[val] = name 
    	  mod = Component::Identifier::Axis
    	  if mod.const_defined? name.intern
    	    IDENTIFIERS[val] = mod.const_get name.intern
  	    end
    	end

      #
      # Reverse lookup on the axis (axiscode to name)
      #
    	def Axis.name_of axiscode
    	  NAMES[axiscode]
  	  end
  	  
  	  def Axis.identifier_of axiscode
  	    IDENTIFIERS[axiscode]
	    end
    end
    
    # 
    # A button is a button on a mouse, a joystick, or some other input device.
    #
    module Button
      B_0 = "0"   
      B_1 = "1" 
      B_2 = "2"
      B_3 = "3"
      B_4 = "4"
      B_5 = "5"
      B_6 = "6"
      B_7 = "7"
      B_8 = "8"
      B_9 = "9"
      B_10 = "10"
      B_11 = "11"
      B_12 = "12"
      B_13 = "13"
      B_14 = "14"
      B_15 = "15"
      B_16 = "16"
      B_17 = "17"
      B_18 = "18"
      B_19 = "19"
      B_20 = "20"
      B_21 = "21"
      B_22 = "22"
      B_23 = "23"
      B_24 = "24"
      B_25 = "25"
      B_26 = "26"
      B_27 = "27"
      B_28 = "28"
      B_29 = "29"
      B_30 = "30"
      B_31 = "31"

      # Joystick trigger button
      #        
      TRIGGER = "Trigger"

      # Joystick thumb button
      #        
      THUMB = "Thumb"

      # Second joystick thumb button
      #        
      THUMB2 = "Thumb 2"

      # Joystick top button
      #        
      TOP = "Top"

      # Second joystick top button
      #        
      TOP2 = "Top 2"

      # The joystick button you play with with you little finger (Pinkie on#that* side
      # of the pond :P)
      #        
      PINKIE = "Pinkie"

      # Joystick button on the base of the device
      #        
      BASE = "Base"

      # Second joystick button on the base of the device
      #        
      BASE2 = "Base 2"

      # Third joystick button on the base of the device
      #        
      BASE3 = "Base 3"

      # Fourth joystick button on the base of the device
      #        
      BASE4 = "Base 4"

      # Fifth joystick button on the base of the device
      #        
      BASE5 = "Base 5"

      # Sixth joystick button on the base of the device
      #        
      BASE6 = "Base 6"

      # erm, dunno, but it's in the defines so it might exist.
      #        
      DEAD = "Dead"

      # 'A' button on a gamepad
      #        
      A = "A"

      # 'B' button on a gamepad
      #        
      B = "B"

      # 'C' button on a gamepad
      #        
      C = "C"

      # 'X' button on a gamepad
      #        
      X = "X"

      # 'Y' button on a gamepad
      #        
      Y = "Y"

      # 'Z' button on a gamepad
      #        
      Z = "Z"

      # Left thumb button on a gamepad
      #        
      LEFT_THUMB = "Left Thumb"

      # Right thumb button on a gamepad
      #        
      RIGHT_THUMB = "Right Thumb"

      # Second left thumb button on a gamepad
      #        
      LEFT_THUMB2 = "Left Thumb 2"

      # Second right thumb button on a gamepad
      #        
      RIGHT_THUMB2 = "Right Thumb 2"

      # 'Select' button on a gamepad
      #        
      SELECT = "Select"

      # 'Mode' button on a gamepad
      #        
      MODE = "Mode"

      # Another left thumb button on a gamepad (how many thumbs do you have??)
      #        
      LEFT_THUMB3 = "Left Thumb 3"

      # Another right thumb button on a gamepad
      #        
      RIGHT_THUMB3 = "Right Thumb 3"

      # Digitiser pen tool button
      #        
      TOOL_PEN = "Pen"

      # Digitiser rubber (eraser) tool button
      #        
      TOOL_RUBBER = "Rubber"

      # Digitiser brush tool button
      #        
      TOOL_BRUSH = "Brush"

      # Digitiser pencil tool button
      #        
      TOOL_PENCIL = "Pencil"

      # Digitiser airbrush tool button
      #        
      TOOL_AIRBRUSH = "Airbrush"

      # Digitiser finger tool button
      #        
      TOOL_FINGER = "Finger"

      # Digitiser mouse tool button
      #        
      TOOL_MOUSE = "Mouse"

      # Digitiser lens tool button
      #        
      TOOL_LENS = "Lens"

      # Digitiser touch button
      #        
      TOUCH = "Touch"

      # Digitiser stylus button
      #        
      STYLUS = "Stylus"

      # Second digitiser stylus button
      #        
      STYLUS2 = "Stylus 2"

      #
      # An unknown button
      #
      UNKNOWN = "Unknown"

      #
      # Returns the back mouse button.
      #
      BACK = "Back"

      #
      # Returns the extra mouse button.
      #
      EXTRA = "Extra"

      #
      # Returns the forward mouse button.
      #
      FORWARD = "Forward"

      #
      # The primary or leftmost mouse button.
      #
      LEFT = "Left"

      #
      # Returns the middle mouse button, not present if the mouse has fewer than three buttons.
      #
      MIDDLE = "Middle"

      #
      # The secondary or rightmost mouse button, not present if the mouse is a single-button mouse.
      #
      RIGHT = "Right"

      #
      # Returns the side mouse button.
      #
      SIDE = "Side"
        
      #
      # A hash storing all the button names
      #
      NAMES = {}
      
      #
      # A hash storing all the identifiers
      #
      IDENTIFIERS = {}
      
    	#
    	# Initializes the NAMES hash. Runs as the module is being initialized.
    	#
    	self.constants.each do |name| 
    	  next if name == "NAMES" or name == "IDENTIFIERS"
        
    	  val = Button.const_get(name.intern);
    	  NAMES[val] = name 
    	  def val.identifier 
    	    Button.identifier_of self
  	    end

    	  mod = Component::Identifier::Button
    	  
    	  if not mod.const_defined? name.intern
    	    puts "Can't find #{name} in Component.Identifier.Button" unless /^B(_.*)$/ =~ name
    	    IDENTIFIERS[val] = mod.java_class.field($1).value(mod.java_class)
  	    else
    	    IDENTIFIERS[val] = mod.const_get name.intern
  	    end
    	end

      #
      # Reverse lookup on the button (buttoncode to name)
      #
    	def Button.name_of buttoncode
    	  NAMES[buttoncode]
  	  end
  	  
  	  def Button.identifier_of buttoncode
  	    IDENTIFIERS[buttoncode]
	    end
    end

    #
    # A key is a button on your keyboard.
    #
    module Key
      VOID = "Void"
      ESCAPE = "Escape"
      K_1 = "1"
      K_2 = "2"
      K_3 = "3"
      K_4 = "4"
      K_5 = "5"
      K_6 = "6"
      K_7 = "7"
      K_8 = "8"
      K_9 = "9"
      K_0 = "0"
      MINUS = "-"
      EQUALS = "="
      BACK = "Back"
      TAB = "Tab"
      Q = "Q"
      W = "W"
      E = "E"
      R = "R"
      T = "T"
      Y = "Y"
      U = "U"
      I = "I"
      O = "O"
      P = "P"
      LBRACKET = "["
      RBRACKET = "]"
      RETURN = "Return"
      LCONTROL = "Left Control"
      A = "A"
      S = "S"
      D = "D"
      F = "F"
      G = "G"
      H = "H"
      J = "J"
      K = "K"
      L = "L"
      SEMICOLON = ";"
      APOSTROPHE = "'"
      GRAVE = "~"
      LSHIFT = "Left Shift"
      BACKSLASH = "\\"
      Z = "Z"
      X = "X"
      C = "C"
      V = "V"
      B = "B"
      N = "N"
      M = "M"
      COMMA = ","
      PERIOD = "."
      SLASH = "/"
      RSHIFT = "Right Shift"
      MULTIPLY = "Multiply"
      LALT = "Left Alt"
      SPACE = " "
      CAPITAL = "Caps Lock"
      F1 = "F1"
      F2 = "F2"
      F3 = "F3"
      F4 = "F4"
      F5 = "F5"
      F6 = "F6"
      F7 = "F7"
      F8 = "F8"
      F9 = "F9"
      F10 = "F10"
      NUMLOCK = "Num Lock"
      SCROLL = "Scroll Lock"
      NUMPAD7 = "Num 7"
      NUMPAD8 = "Num 8"
      NUMPAD9 = "Num 9"
      SUBTRACT = "Num -"
      NUMPAD4 = "Num 4"
      NUMPAD5 = "Num 5"
      NUMPAD6 = "Num 6"
      ADD = "Num +"
      NUMPAD1 = "Num 1"
      NUMPAD2 = "Num 2"
      NUMPAD3 = "Num 3"
      NUMPAD0 = "Num 0"
      DECIMAL = "Num ."
      F11 = "F11"
      F12 = "F12"
      F13 = "F13"
      F14 = "F14"
      F15 = "F15"
      KANA = "Kana"
      CONVERT = "Convert"
      NOCONVERT = "Noconvert"
      YEN = "Yen"
      NUMPADEQUAL = "Num ="
      CIRCUMFLEX = "Circumflex"
      AT = "At"
      COLON = "Colon"
      UNDERLINE = "Underline"
      KANJI = "Kanji"
      STOP = "Stop"
      AX = "Ax"
      UNLABELED = "Unlabeled"
      NUMPADENTER = "Num Enter"
      RCONTROL = "Right Control"
      NUMPADCOMMA = "Num ,"
      DIVIDE = "Num /"
      SYSRQ = "SysRq"
      RALT = "Right Alt"
      PAUSE = "Pause"
      HOME = "Home"
      UP = "Up"
      PAGEUP = "Pg Up"
      LEFT = "Left"
      RIGHT = "Right"
      KEND = "End"
      DOWN = "Down"
      PAGEDOWN = "Pg Down"
      INSERT = "Insert"
      DELETE = "Delete"
      LWIN = "Left Windows"
      RWIN = "Right Windows"
      APPS = "Apps"
      POWER = "Power"
      SLEEP = "Sleep"
      UNKNOWN = "Unknown"

      #
      # A hash storing all the key names
      #
      NAMES = {}
      
      #
      # A hash storing identifiers
      #
      IDENTIFIERS = {}

    	#
    	# Initializes the NAMES hash. Runs as the module is being initialized.
    	#
    	self.constants.each do |name| 
    	  next if name == "NAMES" or name == "IDENTIFIERS"

    	  val = Key.const_get(name.intern);
    	  NAMES[val] = name 
    	  def val.identifier 
    	    Key.identifier_of self
  	    end
    	  
    	  mod = Component::Identifier::Key
    	  name = "END" if name == "KEND"
    	  
    	  if not mod.const_defined? name.intern  	      
      	  puts "Can't find #{name} in Component.Identifier.Button" unless /^K(_.*)$/ =~ name
    	    IDENTIFIERS[val] = mod.java_class.field($1).value(mod.java_class)
    	  else
    	    IDENTIFIERS[val] = mod.const_get name.intern
      	end
    	end

    	def Key.name_of keycode
    	  NAMES[keycode]
  	  end
  	  
  	  def Key.identifier_of keycode
  	    IDENTIFIERS[keycode]
	    end
    end
  end
end