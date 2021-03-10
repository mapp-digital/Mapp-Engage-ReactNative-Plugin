require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNMappPlugin"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                    Mapp SDK for React Native
                   DESC
  s.homepage     = "https://github.com/MappCloud/React-native-plugin"
  s.license      = "MIT"
  s.author      = "Mapp"
  s.platforms    = { :ios => "10.0" }
  s.source       = { :git => "https://github.com/MappCloud/React-native-plugin", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,m,swift}"
  s.vendored_framework = "ios/Frameworks/AppoxeeSDK.framework", "ios/Frameworks/AppoxeeLocationServices.framework", "ios/Frameworks/AppoxeeInapp.framework"
  s.resources = "ios/Frameworks/AppoxeeSDKResources.bundle", "ios/Frameworks/AppoxeeInappResources.bundle"
  s.preserve_path = "ios/Frameworks/"
  # s.public_header_files = "ios/Frameworks/AppoxeeSDK.framework/Headers/"
  s.requires_arc = true
  s.frameworks = "WebKit"
  s.library = 'sqlite3'
  s.dependency "React"

end

