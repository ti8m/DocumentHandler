

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import <QuickLook/QuickLook.h>

@interface DocumentHandler : CDVPlugin <QLPreviewControllerDelegate, QLPreviewControllerDataSource, QLPreviewItem>

@property (strong, nonatomic) NSURL* fileUrl;
@property (readonly) NSURL* previewItemURL;

@end