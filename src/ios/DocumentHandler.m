
#import "DocumentHandler.h"

@implementation DocumentHandler

- (void)HandleDocumentWihtURL:(CDVInvokedUrlCommand*)command;
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];

    NSDictionary* dict = [command.arguments objectAtIndex:0];
    
    NSString* urlStr = dict[@"url"];
    NSLog(@"Got url: %@", urlStr);
    NSURL* url = [NSURL URLWithString:urlStr];
    NSData* dat = [NSData dataWithContentsOfURL:url];
    NSString* fileName = [url lastPathComponent];
    NSString* path = [NSTemporaryDirectory() stringByAppendingPathComponent: fileName];
    NSURL* tmpFileUrl = [[NSURL alloc] initFileURLWithPath:path];
    [dat writeToURL:tmpFileUrl atomically:YES];
    self.fileUrl = tmpFileUrl;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        QLPreviewController* cntr = [[QLPreviewController alloc] init];
        cntr.delegate = self;
        cntr.dataSource = self;
        
        UIViewController* root = [[[UIApplication sharedApplication] keyWindow] rootViewController];
        [root presentViewController:cntr animated:YES completion:nil];
    });

    
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
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
