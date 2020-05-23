#import "FlutterRecordingPlugin.h"
#if __has_include(<flutter_recording/flutter_recording-Swift.h>)
#import <flutter_recording/flutter_recording-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_recording-Swift.h"
#endif

@implementation FlutterRecordingPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterRecordingPlugin registerWithRegistrar:registrar];
}
@end
