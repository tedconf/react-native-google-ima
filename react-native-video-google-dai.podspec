require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-video-google-dai"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-video-google-dai
                   DESC
  s.homepage     = "https://github.com/github_account/react-native-video-google-dai"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Your Name" => "yourname@email.com" }
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/github_account/react-native-video-google-dai.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
	# s.dependency 'AFNetworking', '~> 3.0'
  # s.dependency 'react-native-video', '~> 4.4.1'
  s.dependency 'GoogleAds-IMA-iOS-SDK', '~> 3.9'
  # s.dependency "..."
end

