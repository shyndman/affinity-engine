import "ca.scotthyndman.game.engine.scene.Group"

module Affinity
  module Builder
    def node name, options, &block
      group = Group.new(name, options[:x] || 0, options[:y] || 0)
      group.instance_eval(&block) if block
      group
    end
  end
end
