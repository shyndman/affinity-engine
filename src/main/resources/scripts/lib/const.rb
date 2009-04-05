require 'delegate'

#
# Represents a keycode
#
class Key < DelegateClass(Fixnum)
  def self.metaclass; class << self; self; end; end
  
  attr_reader :code, :name
  
  def initialize(name, code)
    super(name)   
    @code = code
    @name = name
  end
  
  # to trick Hashes
  def hash 
    @code.hash
  end
  
  # to trick numbers
  def eql?(o)
    !o.nil? and o.respond_to? :to_i and o.to_i == to_i
  end
  
  # to trick others
  def to_i
    @code
  end
  
  def to_s
    "#{@name}:#{@code}"
  end

  def to_yaml_properties
    ['@code', '@name']
  end
end

#
# Stores key constants. They are not actually stored as Fixnums, but Keys (as defined above).
#
module Keys 
	NONE = 0x00
	ESCAPE = 0x01
	KEY_1 = 0x02
	KEY_2 = 0x03
	KEY_3 = 0x04
	KEY_4 = 0x05
	KEY_5 = 0x06
	KEY_6 = 0x07
	KEY_7 = 0x08
	KEY_8 = 0x09
	KEY_9 = 0x0A
	KEY_0 = 0x0B
	MINUS = 0x0C
	EQUALS = 0x0D
	BACK = 0x0E
	TAB = 0x0F
	Q = 0x10
	W = 0x11
	E = 0x12
	R = 0x13
	T = 0x14
	Y = 0x15
	U = 0x16
	I = 0x17
	O = 0x18
	P = 0x19
	LBRACKET = 0x1A
	RBRACKET = 0x1B
	RETURN = 0x1C 
	LCONTROL = 0x1D
	A = 0x1E
	S = 0x1F
	D = 0x20
	F = 0x21
	G = 0x22
	H = 0x23
	J = 0x24
	K = 0x25
	L = 0x26
	SEMICOLON = 0x27
	APOSTROPHE = 0x28
	GRAVE = 0x29
	LSHIFT = 0x2A
	BACKSLASH = 0x2B
	Z = 0x2C
	X = 0x2D
	C = 0x2E
	V = 0x2F
	B = 0x30
	N = 0x31
	M = 0x32
	COMMA = 0x33
	PERIOD = 0x34 
	SLASH = 0x35 
	RSHIFT = 0x36
	MULTIPLY = 0x37 
	LMENU = 0x38 
	SPACE = 0x39
	CAPITAL = 0x3A
	F1 = 0x3B
	F2 = 0x3C
	F3 = 0x3D
	F4 = 0x3E
	F5 = 0x3F
	F6 = 0x40
	F7 = 0x41
	F8 = 0x42
	F9 = 0x43
	F10 = 0x44
	NUMLOCK = 0x45
	SCROLL = 0x46 
	NUMPAD7 = 0x47
	NUMPAD8 = 0x48
	NUMPAD9 = 0x49
	SUBTRACT = 0x4A 
	NUMPAD4 = 0x4B
	NUMPAD5 = 0x4C
	NUMPAD6 = 0x4D
	ADD = 0x4E 
	NUMPAD1 = 0x4F
	NUMPAD2 = 0x50
	NUMPAD3 = 0x51
	NUMPAD0 = 0x52
	DECIMAL = 0x53 
	F11 = 0x57
	F12 = 0x58
	F13 = 0x64 
	F14 = 0x65 
	F15 = 0x66 
	KANA = 0x70 
	CONVERT = 0x79 
	NOCONVERT = 0x7B 
	YEN = 0x7D 
	NUMPADEQUALS = 0x8D 
	CIRCUMFLEX = 0x90 
	AT = 0x91 
	COLON = 0x92 
	UNDERLINE = 0x93 
	KANJI = 0x94 
	STOP = 0x95 
	AX = 0x96 
	UNLABELED = 0x97 
	NUMPADENTER = 0x9C 
	RCONTROL = 0x9D
	NUMPADCOMMA = 0xB3 
	DIVIDE = 0xB5 
	SYSRQ = 0xB7
	RMENU = 0xB8 
	PAUSE = 0xC5 
	HOME = 0xC7 
	UP = 0xC8 
	PRIOR = 0xC9 
	LEFT = 0xCB 
	RIGHT = 0xCD 
	KEY_END = 0xCF 
	DOWN = 0xD0 
	NEXT = 0xD1 
	INSERT = 0xD2 
	DELETE = 0xD3 
	LMETA = 0xDB 
	RMETA = 0xDC 
	APPS = 0xDD
	POWER = 0xDE
	SLEEP = 0xDF

  #
  # A hash storing all the key names
  #
  KEY_NAMES = {}
	
	#
	# Run as the module is being initialized
	#
	self.constants.each do |name| 
	  next if name == "KEY_NAMES"
	  
	  val = Key.new(name, Keys.const_get(name.intern));
	  KEY_NAMES[val.code] = name 
	  
	  remove_const(name.intern)
	  Keys.const_set(name.intern, val)
	  val
	end
end

#
# Extension of Symbol to map to Keys
#
class Symbol
  def key
    key = to_s.upcase
    Keys.const_defined?(key.intern) ? Keys.const_get(key.intern) : Keys.const_get(('KEY_' + key).intern)
  end
end

#
# Extension of Fixnum to map to keys
#
class Fixnum
  def key
    key = 'KEY_' + to_s.upcase
    Keys.const_get(key.intern)
  end
end