require 'thread'
require 'fiber'
require 'tracer'
#
# Redefined to include threading
#
class Entity
  
  #
  # The fiber responsible for consuming events off of the event queue
  #
  attr_reader :event_fiber
  
  #
  # The queue to which events are pushed for consumption by this entity
  #
  attr_reader :event_queue
  
  private
  
  #
  # Initializes the threading system
  #
  def initialize_threading
    @worker_pool = nil
    
    
    #
    # Define the entity thread, with its internal fibers
    # 
    @stopped = true
    ent = self
    Thread.abort_on_exception = true
    @entity_thread = Thread.new { 
      Thread.stop
      @worker_pool = FiberWorkerPool.new(ent)
      @event_fiber = Fiber.new {
        run_event_loop
      }
      
      @event_fiber.resume 
    }

    @event_queue = Queue.new
  end
  
  public
  
  #
  # Starts the entity's thread
  # 
  def start
    while not @entity_thread.stop?
      Kernel.sleep 0.1
    end

    @stopped = false    
    @entity_thread.run
  end
  
  #
  # Sleeps the thread
  #
  def stop
    @stopped = true
  end
  
  #
  # Pushes an event onto the queue.
  #
  def push_event event
    @event_queue << event
  end
  
  #
  # Handles a method by routing it appropriately
  #
  def handle_event event    
    case event[:name]
    when 'sleep'
      puts 'sleep'
      sleep 4
      
    when 'change_state'
      puts 'change_state'
      
    when 'tick'
      puts 'tick'
      
    end
  end
  
  #
  # Sleeps for the specified number of seconds before continuing on at the point where this method was called.
  #
  def sleep seconds
    w = Fiber.current.worker
    dt = Time.now.to_i
    f = Finishable.new do
      return (Time.now.to_i - dt) >= seconds
    end
    wait_for f
  end
  
  #
  # Waits for the specified finishable to complete before continuing execution of the state.
  #
  def wait_for finishable, options = nil
    Fiber.yield(finishable, options)
  end
  
  private
  
  #
  # Runs the event loop.
  #
  def run_event_loop
    while true
      event = @event_queue.pop
      @worker_pool.dispatch event
      
      if @stopped
        Thread.stop;
        return
      end
    end
  end
end

#
# A fiber worker is used by entities to perform event dispatching. An entity stores a
# pool of these workers that can be asked to perform a unit of work. However, since 
# some of the operations result in a "block", workers can exist in an unusable state.
# The worker will be asked continuously about whether its waiting task is complete, at
# which point it will be returned to the pool.
#
class FiberWorker
  
  #
  # Initializes the fiber worker with its associated entity
  #
  def initialize entity, pool
    @pool = pool
    @finishable = nil
    @event = nil
    @blocked = false
    @is_running = false
    @fiber = Fiber.new {|event|
      while true
        entity.handle_event event
        event = Fiber.yield nil
      end  
    }
    @fiber.worker = self
  end
  
  #
  # Returns the worker's pool
  #
  def pool; @pool; end
  
  def event; @event; end
  
  #
  # Sets the finishable that is blocking this worker from completing its task.
  #
  def finishable= finishable
    @finishable = finishable
    check_block
  end
  
  #
  # Returns true if the worker is currently blocked
  #
  def blocked?; @blocked; end
  
  #
  # Returns true if the worker is currently running
  #
  def running?; @is_running; end
  
  #
  # Calculates whether the block is still in place
  #
  def check_block
    finished = @finishable.nil? or @finishable.is_finished?
    puts finished
    if finished
      @finishable, @blocked = nil, false
      return @blocked
    end
    
    @blocked = !finished
  end
  
  #
  # Dispatches an event by resuming the fiber.
  #
  def dispatch event
    raise Exception.new("workers cannot dispatch while blocked") if blocked?
    @event = event
    @is_running = true
    
    self.resume event
  end
  
  #
  # Transfers control to this fiber
  #
  def resume event
    f = @fiber.resume event

    if !f.nil? 
      @finishable = f[0];
      return;
    end
    
    @is_running = false
    @event = nil
  end
  
  def inspect
    "Worker(event=#{@event}, running?=#{running?})"
  end
end

#
# A fiber worker pool is a way of being able to dispatch an event without worrying whether
# who should actually do it. More workers are allocated as needed, and things like 
# blocking operations are supported.
#
class FiberWorkerPool
  
  #
  # Initializes a fiber worker pool with the owner entity.
  #
  def initialize entity
    @entity = entity
    @pool = []
    @running = []
    @blocked = []
    @dispatch_fiber = Fiber.new { |event|
      while true
        _dispatch event
        event = Fiber.yield 
      end
    }
  end
  
  #
  # Returns the dispatch fiber
  #
  def fiber
    @dispatch_fiber
  end
  
  #
  # Attempts to dispatch an event
  #
  def dispatch event
    @dispatch_fiber.resume event
  end
  
  private
  
  #
  # The internal dispatching method
  #
  def _dispatch event
    
    #
    # Get a free worker and dispatch the event
    #
    w = get_free_worker
    w.dispatch event
    
    #
    # Update the blocked workers
    #
    update_blocked
    
    #
    # Run any running workers
    #
    finished = @running.find_all do |worker|
      worker.resume worker.event
      !worker.running?
    end
    
    #
    # Remove finished workers, and deal with blocked workers
    #
    @pool += finished
    @blocked -= finished
    @running.clear()
    
    #
    # Add the original worker back to the pool or to the blocked array
    #
    if w.running? # implies blocked
      @blocked << w
    else
      @pool << w
    end
    
    puts "pool    #{@pool.to_s}"
    puts "running #{@running.to_s}"
    puts "blocked #{@blocked.to_s}"
  end

  private
  
  def get_free_worker
    return add_worker if @pool.length == 0
    w = @pool.pop
  end
  
  #
  # Gets all of the running workers that are no longer blocked out of their holding cell
  #
  def update_blocked
    unblocked = @blocked.find_all {|worker| worker.check_block; !worker.blocked?}
    @blocked -= unblocked
    @running += unblocked
  end
  
  #
  # Adds a worker to the pool
  #
  def add_worker
    FiberWorker.new(@entity, self)
  end
end

#
# Represents an operation that can complete.
#
class Finishable
  
  #
  # Initializes a new finishable that marks itself as finished when
  # the provided block returns true
  #
  def initialize &block
    @block = block
  end
  
  #
  # Returns true if the Finishable has completed.
  #
  def is_finished?
    @block.call
  end
end

#
# Adds the ability for a fiber to contain its associated worker.
#
class Fiber
  #
  # Returns the worker associated with this fiber
  #
  def worker
    @worker
  end
  
  #
  # Sets the worker associated with this fiber
  #
  def worker= w
    @worker = w
  end
end

#
# Testing
#
if __FILE__ == $0    
  class Entity
    def initialize
      initialize_threading
    end
  end
  
  @e = Entity.new
  @e.start

  #@e.push_event({name: 'tick'})
  events = [['sleep', 'sleep'], 'tick', 'tick', 'sleep', 'sleep', 'tick', 'tick']
  events.each do |event|
    if event.kind_of?(String)
      event = [event]
    end
    
    event.each do |evt| 
      @e.push_event({name: evt})
    end
    
    sleep 2
  end
  
=begin
  e = Entity.new
  e.start
  
  Thread.list.each {|x| puts "#{x.inspect}: #{x[:name]}" }
  sleep 2
  
  while true
    Thread.list.each {|x| puts "#{x.inspect}: #{x[:name]}" }
    e.push_event({foo: 'bar'})
    e.push_event({foo: 'baz'})
    sleep 2
  end
=end 

end