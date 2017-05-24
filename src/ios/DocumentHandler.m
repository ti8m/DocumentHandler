
#import "DocumentHandler.h"

@implementation DocumentHandler

- (void)HandleDocumentWithURL:(CDVInvokedUrlCommand*)command;
{
    __weak DocumentHandler* weakSelf = self;
    
    dispatch_queue_t asyncQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(asyncQueue, ^{
        
        NSDictionary* dict = [command.arguments objectAtIndex:0];
        
        NSString* urlStr = dict[@"url"];
        NSString* filename = dict[@"fileName"];
        NSURL* url = [NSURL URLWithString:urlStr];
        NSData* dat = [NSData dataWithContentsOfURL:url];
        if (dat == nil) {
          CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:2];
          [weakSelf.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
          return;
        }

        NSString* fileName = [url lastPathComponent];
        
        NSString* fileExt = [fileName pathExtension];
        if([fileExt length] == 0){
            fileName = [NSString stringWithFormat:@"%@%@", fileName, @".pdf"];
        }
        
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
        
        
        CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:filename];
        [weakSelf.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
    });
}

#pragma mark - QLPreviewController data source

- (NSInteger) numberOfPreviewItemsInPreviewController: (QLPreviewController *) controller
{
    return 1;
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
