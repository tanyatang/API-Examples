//
//  LiveStreaming.swift
//  APIExample-SwiftUI
//
//  Created by qinhui on 2024/9/25.
//

import SwiftUI
import Combine

struct LiveStreamingEntry: View {
    @State private var channelName: String = ""
    @State private var channelButtonIsActive = false
    @State private var selectedCamertOption = ""
    @State private var firstFrameToggleIsOn = false
    @State private var preloadIsOn = false
    @State private var roleSheetIsShow = false
    @State private var cameraSheetIsShow = false
    @State private var role: AgoraClientRole = .broadcaster

    @State private var cameraOptions: [AgoraFocalLengthInfo] = []
    @State private var configs: [String: Any] = [:]
    @State var selectedColor: BackgroundColors = .Red

    var agoraKit: AgoraRtcEngineKit = {
        let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        let kit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: nil)
        Util.configPrivatization(agoraKit: kit)
        kit.setLogFile(LogUtils.sdkLogPath())
        return kit
    }()
    
    var body: some View {
        VStack {
            Spacer()
            TextField("Enter channel name".localized, text: $channelName)
                .textFieldStyle(.roundedBorder)
                .padding()
                .padding(.bottom, 10)
            
            //默认背景色
            HStack {
                Text("Default background Color".localized)
                Picker("", selection: $selectedColor) {
                    ForEach(BackgroundColors.allCases) { e in
                        Text(e.suggestedColor).tag(e)
                    }
                }
            }
            
            //首帧出图
            Toggle("First Frame Optimization", isOn: $firstFrameToggleIsOn)
                .padding(.bottom, 10)
                .alert(isPresented: $firstFrameToggleIsOn) {
                    Alert(title: Text("After this function is enabled, it cannot be disabled and takes effect only when both the primary and secondary ends are enabled".localized),
                          primaryButton: .default(Text("Sure".localized), action: {
                                    firstFrameToggleIsOn = true
                                }),
                          secondaryButton: .cancel(Text("Cancel".localized)))
                }
                .fixedSize()
            
            //预加载
            Button(preloadIsOn ? "cancel preload".localized : "preload Channel".localized) {
                preloadIsOn.toggle()
            }
            .padding(.bottom, 10)
    
            //相机
            HStack {
                Text("Camera Selected".localized)
                Button(selectedCamertOption) {
                    self.cameraSheetIsShow = true
                }
                .sheet(isPresented: $cameraSheetIsShow, content: {
                    let params = cameraOptions.flatMap({ $0.value })
                    let keys = params.map({ $0.key })
                    
                    PickerSheetView(selectedOption: $selectedCamertOption, options: keys, isPresented: $cameraSheetIsShow) { selectedOption in
                        self.selectedCamertOption = selectedOption
                        for camera in params {
                            if camera.key == self.selectedCamertOption {
                                let config = AgoraCameraCapturerConfiguration()
                                config.cameraFocalLengthType = camera.value
                                config.cameraDirection = camera.key.contains("Front camera".localized) ? .front : .rear
                                self.agoraKit.setCameraCapturerConfiguration(config)
                                break
                            }
                        }
                    }
                })
            }
            .padding(.bottom, 10)
            
            Button {
                self.roleSheetIsShow = true
            } label: {
                Text("Join".localized)
            }
            .confirmationDialog("Pick Role".localized, isPresented: $roleSheetIsShow, actions: {
                Button("Broadcaster".localized) {
                    self.role = .broadcaster
                    prepareConfig()
                    self.channelButtonIsActive = true
                }
                Button("Audience".localized) {
                    self.role = .audience
                    prepareConfig()
                    self.channelButtonIsActive = true
                }
                Button("Cancel".localized, role: .cancel) {}
            })
            .disabled(channelName.isEmpty)
            
            Spacer()
            NavigationLink(destination: LiveStreaming(configs: configs)
                            .navigationTitle(channelName)
                            .navigationBarTitleDisplayMode(.inline),
                           isActive: $channelButtonIsActive) {
                EmptyView()
            }
            Spacer()
        }
        .onDisappear(perform: {
            UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
        })
        .onAppear(perform: {
            guard let infos = agoraKit.queryCameraFocalLengthCapability() else { return }
            let params = infos.flatMap({ $0.value })
            let keys = params.map({ $0.key })
            cameraOptions = infos
            
            selectedCamertOption = keys.first ?? ""
        })
        .navigationBarTitleDisplayMode(.inline)
    }
    
    func prepareConfig() {
        configs["role"] = self.role
        configs["engine"] = agoraKit
        configs["isFirstFrame"] = firstFrameToggleIsOn
        configs["channelName"] = channelName
        configs["isPreloadChannel"] = preloadIsOn
        configs["cameraKey"] = selectedCamertOption
        configs["backgroundColor"] = selectedColor.value
        print("")
    }
}

struct LiveStreaming: View {
    @ObservedObject private var liveStreamRTCKit = LiveStreamingRTC()
    @State var configs: [String: Any] = [:]
    @State var selectStabilizationMode: AntiShakeLevel = .off
    @State var selectEncodingType: CodeType = .auto
    @State var centerStage: Bool = false
    @State var bFpsState: Bool = false
    @State var waterMarkState: Bool = false
    @State var gasketPushFlow: Bool = false
    @State var showCenterStageAlert: Bool = false
    @State var simulcastStreamState: Bool = false
    @State var simulcastStream: AgoraSimulcastStreamMode = .enableSimulcastStream
    @State private var selectedCamertOption = ""
    @State private var cameraOptions: [AgoraFocalLengthInfo] = []
    @State private var cameraSheetIsShow = false
    @State private var showSnapshot: Bool = false
    @State private var showSnapShotTitle: String = ""
    @State private var showSnapShotMessage: String = ""
    @State private var fastLiveState: Bool = false
    @State private var linkStreamState: Bool = false
    @State var cameraDirection: AgoraCameraDirection = .front
    
    var backgroundView = VideoView(type: .local,
                              audioOnly: false)
    var foregroundView = VideoView(type: .remote,
                               audioOnly: false)
    
    var body: some View {
        ZStack {
            backgroundView
            VStack {
                if liveStreamRTCKit.remoteUid != nil {
                    HStack {
                        Spacer()
                        foregroundView
                            .frame(width: 136, height: 182)
                            .offset(x: -20)
                            .onTapGesture {
                                liveStreamRTCKit.onTapForegroundVideo()
                            }
                    }
                } else {
                    Rectangle()
                        .frame(width: 136, height: 182)
                        .foregroundStyle(.clear)
                }
                
                //防抖
                HStack {
                    Text("anti shake".localized)
                        .foregroundStyle(.white)
                    Picker("", selection: $selectStabilizationMode) {
                        ForEach(AntiShakeLevel.allCases) { e in
                            Text(e.suggestedLevel).tag(e)
                        }
                    }
                    .onChange(of: selectStabilizationMode) { newValue in
                        liveStreamRTCKit.agoraKit.setCameraStabilizationMode(newValue.value)
                    }
                }
                .background(.gray.opacity(0.3))
                .padding(.top, 30)
                
                if liveStreamRTCKit.role == .broadcaster {
                    //centerStage, 相机对焦
                    HStack {
                        Toggle("CenterStage", isOn: $centerStage)
                            .foregroundStyle(.white)
                            .onChange(of: centerStage) { newValue in
                                let centerStageNotSupported = liveStreamRTCKit.agoraKit.isCameraCenterStageSupported()
                                if newValue && !centerStageNotSupported {
                                    showCenterStageAlert = true
                                    return
                                }
                                
                                if newValue {
                                    liveStreamRTCKit.agoraKit.enableCameraCenterStage(newValue)
                                }
                            }
                            .frame(height: 30)
                            .alert(isPresented: $showCenterStageAlert, content: {
                                Alert(
                                    title: Text("This device does not support Center Stage".localized),
                                    dismissButton: .destructive(Text("ok"), action: {
                                        centerStage = false
                                    })
                                )
                            })
                        
                        Text("Camera Selected".localized)
                            .foregroundStyle(.white)
                        Button(selectedCamertOption) {
                            self.cameraSheetIsShow = true
                        }
                        .sheet(isPresented: $cameraSheetIsShow, content: {
                            let params = cameraOptions.flatMap({ $0.value })
                            let keys = params.map({ $0.key })
                            
                            PickerSheetView(selectedOption: $selectedCamertOption, options: keys, isPresented: $cameraSheetIsShow) { selectedOption in
                                self.selectedCamertOption = selectedOption
                                for camera in params {
                                    if camera.key == self.selectedCamertOption {
                                        let config = AgoraCameraCapturerConfiguration()
                                        config.cameraFocalLengthType = camera.value
                                        config.cameraDirection = camera.key.contains("Front camera".localized) ? .front : .rear
                                        if (config.cameraDirection != self.cameraDirection) {
                                            liveStreamRTCKit.agoraKit.switchCamera()
                                        }
                                        liveStreamRTCKit.agoraKit.setCameraCapturerConfiguration(config)
                                        self.cameraDirection = config.cameraDirection
                                        break
                                    }
                                }
                            }
                        })
                    }
                    .fixedSize()
                    .background(.gray.opacity(0.3))
                    
                    //B帧，编码方式
                    HStack {
                        Toggle("B Fps".localized, isOn: $bFpsState)
                            .foregroundStyle(.white)
                            .onChange(of: bFpsState) { newValue in
                                let encoderConfig = AgoraVideoEncoderConfiguration()
                                let videoOptions = AgoraAdvancedVideoOptions()
                                videoOptions.compressionPreference = newValue ? .quality : .lowLatency
                                encoderConfig.advancedVideoOptions = videoOptions
                                
                                liveStreamRTCKit.agoraKit.setVideoEncoderConfiguration(encoderConfig)
                            }
                        Text("Code Type".localized)
                            .foregroundStyle(.white)
                        Picker("", selection: $selectEncodingType) {
                            ForEach(CodeType.allCases) { e in
                                Text(e.suggestedType)
                                    .tag(e)
                            }
                        }
                        .onChange(of: selectEncodingType) { newValue in
                            let encoderConfig = AgoraVideoEncoderConfiguration()
                            let advancedOptions = AgoraAdvancedVideoOptions()
                            advancedOptions.encodingPreference = newValue.value
                            encoderConfig.advancedVideoOptions = advancedOptions
                            
                            liveStreamRTCKit.agoraKit.setVideoEncoderConfiguration(encoderConfig)
                        }
                        .pickerStyle(SegmentedPickerStyle())
                    
                    }
                    .fixedSize()
                    .background(.gray.opacity(0.3))
                    
                    //水印， 垫片推流
                    HStack {
                        Toggle("Water Mark".localized, isOn: $waterMarkState)
                            .foregroundStyle(.white)
                            .onChange(of: waterMarkState) { newValue in
                                if newValue {
                                    if let filepath = Bundle.main.path(forResource: "agora-logo", ofType: "png") {
                                        if let url = URL.init(string: filepath) {
                                            let waterMark = WatermarkOptions()
                                            waterMark.visibleInPreview = true
                                            waterMark.positionInPortraitMode = CGRect(x: 10, y: 80, width: 60, height: 60)
                                            waterMark.positionInLandscapeMode = CGRect(x: 10, y: 80, width: 60, height: 60)
                                            liveStreamRTCKit.agoraKit.addVideoWatermark(url, options: waterMark)
                                        }
                                    }
                                } else {
                                    liveStreamRTCKit.agoraKit.clearVideoWatermarks()
                                }

                            }
                        
                        Toggle("Gasket push flow".localized, isOn: $gasketPushFlow)
                            .foregroundStyle(.white)
                            .onChange(of: gasketPushFlow) { newValue in
                                let options = AgoraImageTrackOptions()
                                let imgPath = Bundle.main.path(forResource: "agora-logo", ofType: "png")
                                options.imageUrl = imgPath
                                
                                liveStreamRTCKit.agoraKit.enableVideoImageSource(newValue, options: options)
                            }

                    }
                    .fixedSize()
                    .background(.gray.opacity(0.3))
                }
                
                //截图，大小流
                HStack {
                    Button("screenshot".localized) {
                        showSnapshot = true

                        guard let remoteUid = liveStreamRTCKit.remoteUid else {
                            showSnapShotTitle = "remote user has not joined, and cannot take a screenshot".localized;
                            showSnapShotMessage = ""
                            return
                        }
                        
                        let path = NSTemporaryDirectory().appending("1.png")
                        liveStreamRTCKit.agoraKit.takeSnapshot(Int(remoteUid), filePath: path)
                        showSnapShotTitle = "Screenshot successful".localized
                        showSnapShotMessage = path
                    }
                    .alert(isPresented: $showSnapshot, content: {
                        Alert(title: Text(showSnapShotTitle), message: Text(showSnapShotMessage))
                    })
                    .padding(.trailing, 20)
                    
                    Toggle(isOn: $simulcastStreamState, label: {
                        VStack {
                            Text("Simulcast Stream Title".localized)
                            Text(simulcastStreamState ? "Opened State".localized : "Default State".localized)
                                .font(.system(size: 11))
                        }
                        .foregroundStyle(.white)
                    })
                    .onChange(of: simulcastStreamState) { newValue in
                        simulcastStreamState = newValue
                        liveStreamRTCKit.agoraKit.setDualStreamMode(newValue ? .enableSimulcastStream : .disableSimulcastStream)
                    }
                }
                .fixedSize()
                .background(.gray.opacity(0.3))
                
                //极速直播, 连麦
                HStack {
                    if liveStreamRTCKit.showUltraLowEntry {
                        Toggle("Fast Live".localized, isOn: $fastLiveState)
                            .onChange(of: fastLiveState) { newValue in
                                liveStreamRTCKit.onToggleUltraLowLatency(enabled: newValue)
                            }
                            .disabled(linkStreamState)
                    }
                    
                        
                    if liveStreamRTCKit.showLinkStreamEntry {
                        Toggle("Link Stream".localized, isOn: $linkStreamState)
                            .onChange(of: linkStreamState) { newValue in
                                liveStreamRTCKit.onToggleClientRole(state: newValue)
                            }
                    }
                }
                .fixedSize()
                .background(.gray.opacity(0.3))
                
                Spacer()
            }
        }
        .onAppear {
            liveStreamRTCKit.setupRTC(configs: configs, localView: backgroundView.videoView, remoteView: foregroundView.videoView)
            guard let agoraKit = liveStreamRTCKit.agoraKit else { return }
            guard let infos = agoraKit.queryCameraFocalLengthCapability() else { return }
            
            let params = infos.flatMap({ $0.value })
            cameraOptions = infos
            
            if let cameraKey = configs["cameraKey"] as? String {
                selectedCamertOption = cameraKey
                cameraDirection = cameraKey.contains("Front camera".localized) ? .front : .rear
            }
            
            
        }.onDisappear {
            liveStreamRTCKit.leaveChannel()
        }
    }
}

extension AgoraFocalLengthInfo {
    var value: [String: AgoraFocalLength] {
        let title = cameraDirection == 1 ? "Front camera".localized + " - " : "Rear camera".localized + " - "
        switch focalLengthType {
        case .default: return [title + "Default".localized: focalLengthType]
        case .wide: return [title + "Wide".localized: focalLengthType]
        case .ultraWide: return [title + "Length Wide".localized: focalLengthType]
        case .telephoto: return [title + "Telephoto".localized: focalLengthType]
        @unknown default: return [title + "Default".localized: focalLengthType]
        }
    }
}

enum BackgroundColors: String, CaseIterable, Identifiable {
    case Red
    case Blue
    case Pink
    case Purple
    case Yellow

    var id: String { self.rawValue }
    
    var value: UInt32 {
        switch self {
        case .Red:
            return UInt32(0xff0d00ff)
        case .Blue:
            return UInt32(0x0400ffff)
        case .Pink:
            return UInt32(0xff006aff)
        case .Purple:
            return UInt32(0xff00d9ff)
        case .Yellow:
            return UInt32(0xeaff00ff)
        }
    }
    
    var suggestedColor: String {
        switch self {
        case .Red: return "Red".localized
        case .Blue: return "Blue".localized
        case .Pink: return  "Pink".localized
        case .Purple: return "Purple".localized
        case .Yellow: return "Yellow".localized
        }
    }
}

enum AntiShakeLevel: String, CaseIterable, Identifiable {
    case off
    case auto
    case level1
    case level2
    case level3

    var id: String { self.rawValue }
    
    var value: AgoraCameraStabilizationMode {
        switch self {
        case .off:
            return .off
        case .auto:
            return .auto
        case .level1:
            return .level1
        case .level2:
            return .level2
        case .level3:
            return .level3
        }
    }
    
    var suggestedLevel: String {
        return self.rawValue
    }
}

enum CodeType: String, CaseIterable, Identifiable {
    case auto
    case software
    case hardware
    
    var id: String { self.rawValue }
    
    var value: AgoraEncodingPreference {
        switch self {
        case .auto:
            return .preferAuto
        case .software:
            return .prefersoftware
        case .hardware:
            return .preferhardware
        }
    }
    
    var suggestedType: String {
        switch self {
        case .auto: return "auto".localized
        case .software: return "software".localized
        case .hardware: return  "hardware".localized
        }
    }
}

#Preview {
    LiveStreaming()
}
