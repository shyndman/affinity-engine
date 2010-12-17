import "ca.scotthyndman.game.engine.scene.Graphic"

module Affinity
  module Builder
    def graphic(texture, options = nil, &block)
      graphic = Graphic.new(texture, options[:x] || 0, options[:y] || 0)
      graphic.instance_eval(&block) if block
      graphic
    end
  end
end