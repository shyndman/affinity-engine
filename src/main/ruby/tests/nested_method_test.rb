class NestedMethods  
  def inner_method_b
    puts "fake inner_method_b"
  end
  
  def outer_method_a
    def inner_method
      puts "inner_method a"
    end
    
    self.inner_method
  end
  
  def outer_method_b
    def inner_method
      puts "inner_method b"
    end
    
    self.inner_method
  end
end

nm = NestedMethods.new
nm.outer_method_a
nm.outer_method_b
nm.outer_method_a
nm.outer_method_a