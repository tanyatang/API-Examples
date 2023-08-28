//
//  ViewController.m
//  APIExample
//
//  Created by zhaoyongqiang on 2023/7/11.
//

#import "ViewController.h"
#import "BaseViewController.h"
#import <AgoraRtcKit/AgoraRtcKit.h>

@interface MenuItem : NSObject
@property(nonatomic, copy)NSString *name;
@property(nonatomic, copy)NSString *entry;
@property(nonatomic, copy)NSString *storyboard;
@property(nonatomic, copy)NSString *controller;
@property(nonatomic, copy)NSString *note;

@end
@implementation MenuItem

-(instancetype)initWithName: (NSString *)name storyboard: (NSString *)storyboard controller: (NSString *)controller {
    self.name = name;
    self.storyboard = storyboard;
    self.controller = controller;
    self.entry = @"EntryViewController";
    return self;
}

@end

@interface MenuSection : NSObject

@property(nonatomic, copy)NSString *name;
@property(nonatomic, strong)NSArray<MenuItem *> *rows;

@end
@implementation MenuSection

- (instancetype)initWithName: (NSString *)name rows: (NSArray<MenuItem *> *)rows {
    self.name = name;
    self.rows = rows;
    return self;
}

+ (NSArray *)menus {
    NSMutableArray *array = [NSMutableArray array];
    MenuSection *channelSection = [[MenuSection alloc] initWithName:@"Channel" rows:@[
        [[MenuItem alloc]initWithName:@"MultipleChannels".localized storyboard:@"JoinMultiChannel" controller:@""],
    ]];
    [array addObject:channelSection];
    
    MenuSection *basicSection = [[MenuSection alloc] initWithName:@"Audio" rows:@[
        [[MenuItem alloc]initWithName:@"AudioBasic".localized storyboard:@"JoinChannelAudio" controller:@""],
        [[MenuItem alloc]initWithName:@"CustomAudioCapture".localized storyboard:@"CustomPcmAudioSource" controller:@""],
        [[MenuItem alloc]initWithName:@"AudioPrePostProcess".localized storyboard:@"VoiceChanger" controller:@""],
        [[MenuItem alloc]initWithName:@"SpatialAudio".localized storyboard:@"SpatialAudio" controller:@""],
        [[MenuItem alloc]initWithName:@"RawAudioData".localized storyboard:@"RawAudioData" controller:@""],
        [[MenuItem alloc]initWithName:@"CustomAudioRender".localized storyboard:@"CustomAudioRender" controller:@""],
        [[MenuItem alloc]initWithName:@"AudioFilePlayback".localized storyboard:@"AudioMixing" controller:@""],
    ]];
    [array addObject:basicSection];
    MenuSection *videoSection = [[MenuSection alloc] initWithName:@"Video" rows:@[
        [[MenuItem alloc]initWithName:@"VideoBasic(Token)".localized storyboard:@"JoinChannelVideoToken" controller:@""],
        [[MenuItem alloc]initWithName:@"VideoBasic".localized storyboard:@"JoinChannelVideo" controller:@""],
        [[MenuItem alloc]initWithName:@"ScreenSharing".localized storyboard:@"ScreenShare" controller:@""],
        [[MenuItem alloc]initWithName:@"VideoPrePostProcess".localized storyboard:@"VideoProcess" controller:@""],
        [[MenuItem alloc]initWithName:@"Content Inspect".localized storyboard:@"ContentInspect" controller:@""],
        [[MenuItem alloc]initWithName:@"VideoAdvanced".localized storyboard:@"LiveStreaming" controller:@""],
        [[MenuItem alloc]initWithName:@"CustomVideoRenderer".localized storyboard:@"CustomVideoRender" controller:@""],
        [[MenuItem alloc]initWithName:@"RawVideoData".localized storyboard:@"RawVideoData" controller:@""],
        [[MenuItem alloc]initWithName:@"CustomVideoCapture".localized storyboard:@"CustomVideoSourcePush" controller:@""],
        [[MenuItem alloc]initWithName:@"PictureInPicture".localized storyboard:@"PictureInPicture" controller:@""],
    ]];
    [array addObject:videoSection];
    MenuSection *playerSection = [[MenuSection alloc] initWithName:@"Player" rows:@[
        [[MenuItem alloc]initWithName:@"Media Player".localized storyboard:@"MediaPlayer" controller:@""],
        [[MenuItem alloc]initWithName:@"VirtualMetronome".localized storyboard:@"RhythmPlayer" controller:@""],
    ]];
    [array addObject:playerSection];
    MenuSection *recordingSection = [[MenuSection alloc] initWithName:@"Media Recording" rows:@[
        [[MenuItem alloc]initWithName:@"Recording".localized storyboard:@"JoinChannelVideoRecorder" controller:@""],
    ]];
    [array addObject:recordingSection];
    MenuSection *streamingSection = [[MenuSection alloc] initWithName:@"Media Streaming" rows:@[
        [[MenuItem alloc]initWithName:@"MediaPush".localized storyboard:@"RTMPStreaming" controller:@""],
        [[MenuItem alloc]initWithName:@"DirectCDNStreaming".localized storyboard:@"FusionCDN" controller:@""],
        [[MenuItem alloc]initWithName:@"MediaRelay".localized storyboard:@"MediaChannelRelay" controller:@""],
    ]];
    [array addObject:streamingSection];
    MenuSection *affiliatedSection = [[MenuSection alloc] initWithName:@"Media Affiliated Information" rows:@[
        [[MenuItem alloc]initWithName:@"Metadata(SEI)".localized storyboard:@"VideoMetadata" controller:@""],
        [[MenuItem alloc]initWithName:@"DataStream".localized storyboard:@"CreateDataStream" controller:@""],
    ]];
    [array addObject:affiliatedSection];
    MenuSection *deviceSection = [[MenuSection alloc] initWithName:@"Device Manager" rows:@[
        [[MenuItem alloc]initWithName:@"MobileCameraDevice".localized storyboard:@"MutliCamera" controller:@""],
    ]];
    [array addObject:deviceSection];
    MenuSection *plugInSection = [[MenuSection alloc] initWithName:@"Plug-in" rows:@[
        [[MenuItem alloc]initWithName:@"Extension".localized storyboard:@"SimpleFilter" controller:@""],
    ]];
    [array addObject:plugInSection];
    MenuSection *networkSection = [[MenuSection alloc] initWithName:@"Network" rows:@[
        [[MenuItem alloc]initWithName:@"Encryption".localized storyboard:@"StreamEncryption" controller:@""],
    ]];
    [array addObject:networkSection];
    return array.copy;
}

@end

@interface ViewController ()<UITableViewDelegate, UITableViewDataSource>
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong)NSArray<MenuSection *> *datas;

@end

@implementation ViewController

- (NSArray<MenuSection *> *)datas {
    return [MenuSection menus];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = @"Agora API Example";
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.tableView.tableFooterView = [UIView new];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.datas.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.datas[section].rows.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell" forIndexPath:indexPath];
    cell.textLabel.text = self.datas[indexPath.section].rows[indexPath.row].name;
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return self.datas[section].name;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    MenuItem *item = self.datas[indexPath.section].rows[indexPath.row];
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:item.storyboard bundle:nil];
    
    BaseViewController *controller = [storyboard instantiateViewControllerWithIdentifier:item.entry];
    controller.title = item.name;
    [self.navigationController pushViewController:controller animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    if (section == self.datas.count - 1) { return 100;}
    return 0;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    if (section != self.datas.count - 1) { return nil;}
    UILabel *lable = [[UILabel alloc] init];
    lable.font = [UIFont systemFontOfSize:16 weight:1.5];
    lable.text = [NSString stringWithFormat:@"SDK Version: %@",[AgoraRtcEngineKit getSdkVersion]];
    lable.textAlignment = NSTextAlignmentCenter;
    return lable;
}

@end
