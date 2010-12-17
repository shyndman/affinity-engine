
class A
  
  def initialize(*args, &block)
    puts "A"
  end
  
  def old_initialize(*args, &block)
    puts "old_initialize"
  end
end

class B < A  
  def initialize
    super
    puts "B"
  end
end

B.new