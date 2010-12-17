import "com.jme.util.geom.BufferUtils"
import "com.jme.scene.Line"
import "com.jme.scene.QuadMesh"
import "com.jme.scene.TexCoords"
import "java.nio.IntBuffer"

module Affinity
  module Builder
    #
    # Draws a polygon
    #
    # points - an array of x, y pairs, alternating
    #
    # :name = the name of the shape
    # :filled = true/false
    # :colors = array of colors (ColorRGBA[])
    # :color = the one color used, if :colors not specified (ColorRGBA)
    # :texture_points = an array of alternating x, y pairs
    # :line_width = the width of the line in pixels (only if not filled)
    #
    def polygon points, options, &block
      return polyline unless options.has_key? :filled
      
      # build the quad
      quad = QuadMesh.new options[:name]

      # build up a points float buffer
      pbuf = BufferUtils.createVector3Buffer(points.length / 2)
      c = 0
      points.each do |v|
        pbuf.put v
        c += 1
        pbuf.put 0 if c % 2 == 0
      end
      
      # set up the colors
      if options.has_key? :colors
        quad.set_color_buffer(BufferUtils.create_float_buffer(options[:colors]))
      elsif options.has_key? :color
        quad.set_default_color(options[:color])
      end
      
      #textures
      if options.has_key? :texture_points
          buffer = BufferUtils.createVector2Buffer options[:texture_points].length
          buffer.put options[:texture_points]
          quad.set_texture_coords(TexCoords.new(buffer))
      end
      
      # index buffer
      ibuf = BufferUtils.createIntBuffer((0..(points.length / 2) - 1).to_a)
      
      # finish building the quad
      quad.reconstruct(pbuf, nil, quad.get_color_buffer, quad.get_texture_coords)
      quad.set_index_buffer ibuf

      # block invoke      
      quad.instance_eval(&block) if block
      
      quad
    end
    
    #
    # Draws a polyline
    #
    # points - an array of x, y pairs, alternating
    #
    # :name = the name of the shape
    # :colors = array of colors (ColorRGBA[])
    # :color = the one color used, if :colors not specified (ColorRGBA)
    # :texture_points = an array of alternating x, y pairs
    # :line_width = the width of the line in pixels
    #
    def polyline points, options, &block
      
      # build a line
      line = Line.new options[:name]
      
      # build up a points float buffer
      pbuf = BufferUtils.createVector3Buffer(points.length / 2)
      puts "arrived"
      c = 0
      points.each do |v|
        pbuf.put v
        c += 1
        pbuf.put 0 if c % 2 == 0
      end
      
      # set the width
      if options.has_key? :line_width
        line.set_line_width(options[:line_width])
      end
      
      # set up the colors
      if options.has_key? :colors
        line.set_color_buffer(BufferUtils.create_float_buffer(options[:colors].to_java ColorRGBA))
      elsif options.has_key? :color
        line.set_default_color(options[:color])
      end
      
      #textures
      if options.has_key? :texture_points
          buffer = BufferUtils.createVector2Buffer options[:texture_points].length
          buffer.put options[:texture_points]
          line.set_texture_coords(TexCoords.new(buffer))
      end
      
      # index buffer
      ibuf = BufferUtils.createIntBuffer((0..(points.length / 2) - 1).to_a.to_java :int)
      
      # finish building the quad
      line.reconstruct(pbuf, nil, line.get_color_buffer, line.get_texture_coords[0])
      line.set_index_buffer ibuf
      
      # block invoke
      line.instance_eval(&block) if block
      
      line
    end
  end
end