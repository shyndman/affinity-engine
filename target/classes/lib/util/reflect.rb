
class MethodInfo
  PUBLIC = 0
  PROTECTED = 1
  PRIVATE =2    
  
  attr_reader :cls, :name, :access
  
  def initialize cls, name, access
    @cls, @name, @access = cls, name, access
  end
end

class Introspector
  
  def Introspector.get_method_info cls, name
    name = name.intern unless name.kind_of? Symbol
    access = get_access(cls, name)
    MethodInfo.new(cls, name, access)
  end
  
  private
  def self.get_access cls, name
    if cls.public_instance_methods.include? name
      return MethodInfo::PUBLIC
    elsif cls.protected_instance_methods.include? name
      return MethodInfo::PROTECTED
    end
    
    return MethodInfo::PRIVATE
  end
end

if __FILE__ == $0
  puts Introspector.get_method_info(Array, :to_s)
end