import "com.jme.util.TextureManager"
import "com.jme.util.resource.ResourceLocatorTool"

module Affinity
  module Builder
    #
    # Loads and returns the texture specified
    #
    def tex(path, options = {})      
      if options.has_key?(:classpath)
        path = ResourceLocatorTool.locateResource(ResourceLocatorTool::TYPE_TEXTURE, path)
      end
      
      TextureManager.loadTexture path
    end
  end
end

