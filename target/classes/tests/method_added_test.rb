class MethodWatcher
  def self.method_added p
    puts "method_added name=#{p}, self=#{self}"
  end
  
  def foo 
  end
end

class Subclass < MethodWatcher
  def subclass_method
    
  end
end