require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNMappPlugin"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                    Mapp SDK for React Native
                   DESC
  s.homepage     = "https://github.com/mapp-digital/Mapp-Engage-ReactNative-Plugin"
  s.license      = "MIT"
  s.author      = "Mapp"
  s.platforms    = { :ios => "15.1" }
  s.source       = { :git => "https://github.com/mapp-digital/Mapp-Engage-ReactNative-Plugin", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,m,mm,swift}"
  s.exclude_files = "ios/RNMappPluginTests/**/*"
  # AppoxeeInapp already contains the push SDK binary. Adding AppoxeeSDK here
  # produces duplicate symbols, so it remains preserved for headers/resources only.
  s.vendored_frameworks = "ios/Frameworks/AppoxeeLocationServices.xcframework", "ios/Frameworks/AppoxeeInapp.xcframework"
  s.resources = "ios/Frameworks/AppoxeeSDKResources.bundle", "ios/Frameworks/AppoxeeInappResources.bundle"
  s.preserve_path = "ios/Frameworks/"
  s.public_header_files = "ios/Frameworks/AppoxeeSDK.xcframework/ios-arm64/Headers/", "ios/Frameworks/AppoxeeLocationServices.xcframework/ios-arm64/Headers/", "ios/Frameworks/AppoxeeInapp.xcframework/ios-arm64/Headers/"
  s.requires_arc = true
  s.frameworks = "WebKit"
  s.library = 'sqlite3'
  if respond_to?(:install_modules_dependencies, true)
    install_modules_dependencies(s)
  else
    s.dependency "React-Core"
  end

end
