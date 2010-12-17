
class A
  def a
    puts "a"
  end
  
#  alias_method :b, :a
 # remove_method :a
end

class B < A
  def a
    super
    puts "a_B"
  end
  
  alias_method :b, :a
end

B.new.b
