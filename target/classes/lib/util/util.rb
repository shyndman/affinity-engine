module Affinity
  module Util
    def self.load_dir(dir, options = {})
      files = Dir.glob("#{$basedir}#{dir}/*.rb")
      if options[:except]
        files -= files.reject(&options[:except])
      end
    
      if options[:first]
        if options[:first].kind_of? Array
          files.delete_if {|item| options[:first].include? File.basename(item)}
          options[:first].reverse.each {|element| files.insert(0, "#{$basedir}#{dir}/#{element}")}
        else
          files.delete_if {|item| item =~ /#{options[:first]}$/} 
          files = files.insert(0, "#{$basedir}#{dir}/#{options[:first]}")
        end
      end
    
      type = options[:type] ? "#{options[:type]}::" : ""
    
      files.each do |filename|
        load "#{filename}"
        puts "#{type}#{File.basename(filename)} loaded"
      end
    end
  end
end