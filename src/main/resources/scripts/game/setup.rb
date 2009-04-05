
class PlayerEntity < Entity
  
  attr_accessor :x, :y
  
  def initialize 
    super
  end
  
  on_event :move_left do |info|
    puts "moving left #{self.inspect} #{info.inspect}"
  end
  
  on_event :move_right do |info|
    puts "moving right #{self.inspect} #{info.inspect}"
  end 
end