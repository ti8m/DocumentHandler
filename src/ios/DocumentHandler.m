
#import "DocumentHandler.h"

@implementation DocumentHandler

static NSNumber *orientation = nil;

+ (NSNumber*) orientation {
    @synchronized(self) { return orientation; }
}
+ (void) setOrientation:(NSNumber*)val {
    @synchronized(self) { orientation = val; }
}

- (void)HandleDocumentWithURL:(CDVInvokedUrlCommand*)command;
{
    
    orientation = [NSNumber numberWithInt:[[UIDevice currentDevice] orientation]];
    NSLog(@"setting orientation to");
    NSLog(@"%@", orientation);
    
    __weak DocumentHandler* weakSelf = self;
    
    dispatch_queue_t asyncQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(asyncQueue, ^{
        
        NSDictionary* dict = [command.arguments objectAtIndex:0];
        
        NSString* urlStr = dict[@"url"];
        NSURL* url = [NSURL URLWithString:urlStr];
        NSData* dat = [NSData dataWithContentsOfURL:url];
        if (dat == nil) {
          CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:2];
          [weakSelf.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
          return;
        }

        NSString* fileName = [url lastPathComponent];
        NSString* path = [NSTemporaryDirectory() stringByAppendingPathComponent: fileName];
        NSURL* tmpFileUrl = [[NSURL alloc] initFileURLWithPath:path];
        [dat writeToURL:tmpFileUrl atomically:YES];
        weakSelf.fileUrl = tmpFileUrl;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            QLPreviewController* cntr = [[QLPreviewController alloc] init];
            cntr.delegate = weakSelf;
            cntr.dataSource = weakSelf;
            
            UIViewController* root = [[[UIApplication sharedApplication] keyWindow] rootViewController];
            [root presentViewController:cntr animated:YES completion:nil];
        });
        
        
        CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
        [weakSelf.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
    });
}

#pragma mark - QLPreviewController data source

- (NSInteger) numberOfPreviewItemsInPreviewController: (QLPreviewController *) controller
{
    return 1;
}

- (void)previewControllerWillDismiss:(QLPreviewController *)controller {
    if (orientation.intValue == 3) {
        orientation = [NSNumber numberWithInt:4];
    } else if (orientation.intValue == 4) {
        orientation = [NSNumber numberWithInt:4];
    } else if (orientation.intValue == 5) {
        orientation = [NSNumber numberWithInt:4];
    } else if (orientation.intValue == 6) {
        orientation = [NSNumber numberWithInt:3];
    } else {
        orientation = [NSNumber numberWithInt:3];
    }
    
    NSNumber *currentOrientation = [NSNumber numberWithInt:[[UIDevice currentDevice] orientation]];
    
    if (currentOrientation.intValue == 1 || currentOrientation.intValue == 2) {
        [[UIDevice currentDevice] setValue:orientation forKey:@"orientation"];
    }
}

- (void)previewControllerDidDismiss:(QLPreviewController *)controller {
    NSLog(@"handler dismiss");
    NSString *pluginReadyJSCallbackCommand = [NSString stringWithFormat:@"cordova.fireDocumentEvent('documentHandlerOnDismiss');"];
    [self.commandDelegate evalJs:pluginReadyJSCallbackCommand];
}

- (id <QLPreviewItem>) previewController: (QLPreviewController *) controller previewItemAtIndex: (NSInteger) index
{
    return self;
}

#pragma mark - QLPreviewItem protocol

- (NSURL*)previewItemURL
{
    return self.fileUrl;
}

@end
