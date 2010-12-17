$basedir = "./"

def import java_class
  puts "Importing #{java_class}"
  simple_name = java_class.split(".").last
  return const_get(simple_name.intern) if Module.constants.include?(simple_name.intern)
  
  # define the class
  cls = Class.new
  def cls.new(*args)
    
  end
  def cls.method_missing(symbol, *args)
    puts "#{self}.#{symbol}() - fake class"
  end
  
  def cls.const_missing(name)
    puts "#{self}::#{name} - fake class"
    name
  end
  
  # set a shortcut in the Kernel
  Kernel.const_set(simple_name.intern, cls)  
  cls
end

if $0 == __FILE__
  import "com.snaz.snoop.Dragon"
end