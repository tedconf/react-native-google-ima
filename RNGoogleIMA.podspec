require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNGoogleIMA"
  s.version      = package["version"]
  s.summary      = package["summary"]
  s.description  = package["description"]
  s.homepage     = "https://github.com/tedconf/react-native-google-ima"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Sid Ferreira" => "sid@ted.com" }
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/tedconf/react-native-google-ima.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency 'GoogleAds-IMA-iOS-SDK', '~> 3.11.2'
end

