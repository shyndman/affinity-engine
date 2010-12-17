module Affinity
  class Entity
    # 
    # Returns the metaclass for this class.
    #  
    def self.metaclass; class << self; self; end; end
  end
end