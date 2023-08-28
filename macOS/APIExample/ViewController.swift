//
//  ViewController.swift
//  APIExample
//
//  Created by 张乾泽 on 2020/8/28.
//  Copyright © 2020 Agora Corp. All rights reserved.
//

import Cocoa

struct MenuItem {
    var name: String
    var identifier: String
    var controller: String?
    var storyboard: String?
}

class MenuController: NSViewController {
    
    let settings = MenuItem(name: "Global settings".localized, identifier: "menuCell", controller: "Settings", storyboard: "Settings")
    
    var menus:[MenuItem] = [
        // Channel
        MenuItem(name: "Channel", identifier: "headerCell"),
        MenuItem(name: "Quick Switch Channel".localized, identifier: "menuCell", controller: "QuickSwitchChannel", storyboard: "QuickSwitchChannel"),
        MenuItem(name: "MultipleChannels".localized, identifier: "menuCell", controller: "JoinMultipleChannel", storyboard: "JoinMultiChannel"),
        
        // Audio
        MenuItem(name: "Audio", identifier: "headerCell"),
        MenuItem(name: "AudioBasic".localized, identifier: "menuCell", controller: "JoinChannelAudio", storyboard: "JoinChannelAudio"),
        MenuItem(name: "CustomAudioCapture".localized, identifier: "menuCell", controller: "CustomAudioSource", storyboard: "CustomAudioSource"),
        MenuItem(name: "CustomAudioRender".localized, identifier: "menuCell", controller: "CustomAudioRender", storyboard: "CustomAudioRender"),
        MenuItem(name: "RawAudioData".localized, identifier: "menuCell", controller: "RawAudioData", storyboard: "RawAudioData"),
        MenuItem(name: "SpatialAudio".localized, identifier: "menuCell", controller: "SpatialAudio", storyboard: "SpatialAudio"),
        MenuItem(name: "AudioFilePlayback".localized, identifier: "menuCell", controller: "AudioMixing", storyboard: "AudioMixing"),
        MenuItem(name: "AudioPrePostProcess".localized, identifier: "menuCell", controller: "VoiceChanger", storyboard: "VoiceChanger"),
        
        // Video
        MenuItem(name: "Video", identifier: "headerCell"),
        MenuItem(name: "VideoBasic(Token)".localized, identifier: "menuCell", controller: "JoinChannelVideoToken", storyboard: "JoinChannelVideoToken"),
        MenuItem(name: "VideoBasic".localized, identifier: "menuCell", controller: "JoinChannelVideo", storyboard: "JoinChannelVideo"),
        MenuItem(name: "VideoAdvanced".localized, identifier: "menuCell", controller: "LiveStreaming", storyboard: "LiveStreaming"),
        MenuItem(name: "CustomVideoCapture".localized, identifier: "menuCell", controller: "CustomVideoSourcePush", storyboard: "CustomVideoSourcePush"),
        MenuItem(name: "CustomVideoCapture(Multi)".localized, identifier: "menuCell", controller: "CustomVideoSourcePushMulti", storyboard: "CustomVideoSourcePushMulti"),
        MenuItem(name: "CustomVideoRender".localized, identifier: "menuCell", controller: "CustomVideoRender", storyboard: "CustomVideoRender"),
        MenuItem(name: "RawVideoData".localized, identifier: "menuCell", controller: "RawVideoData", storyboard: "RawVideoData"),
        MenuItem(name: "ScreenSharing".localized, identifier: "menuCell", controller: "ScreenShare", storyboard: "ScreenShare"),
        MenuItem(name: "Local composite graph".localized, identifier: "menuCell", controller: "LocalCompositeGraph", storyboard: "LocalCompositeGraph"),
        MenuItem(name: "VideoPrePostProcess".localized, identifier: "menuCell", controller: "Video Process", storyboard: "VideoProcess"),
        MenuItem(name: "Content Inspect".localized, identifier: "menuCell", controller: "ContentInspect", storyboard: "ContentInspect"),
        MenuItem(name: "Multi Camera Sourece".localized, identifier: "menuCell", controller: "MultiCameraSourece", storyboard: "MultiCameraSourece"),
        
        // Player
        MenuItem(name: "Player", identifier: "headerCell"),
        MenuItem(name: "Media Player".localized, identifier: "menuCell", controller: "MediaPlayer", storyboard: "MediaPlayer"),
        
        // Media Recording
        MenuItem(name: "Media Recording", identifier: "headerCell"),
        MenuItem(name: "Recording".localized, identifier: "menuCell", controller: "JoinChannelVideoRecorder", storyboard: "JoinChannelVideoRecorder"),
        
        // Media Streaming
        MenuItem(name: "Media Streaming", identifier: "headerCell"),
        MenuItem(name: "MediaPush".localized, identifier: "menuCell", controller: "RTMPStreaming", storyboard: "RTMPStreaming"),
        MenuItem(name: "DirectCDNStreaming".localized, identifier: "menuCell", controller: "ChannelMediaRelay", storyboard: "ChannelMediaRelay"),

        // Media Affiliated Information
        MenuItem(name: "Media Affiliated Information", identifier: "headerCell"),
        MenuItem(name: "DataStream".localized, identifier: "menuCell", controller: "CreateDataStream", storyboard: "CreateDataStream"),
        
        // Plug-in
        MenuItem(name: "Plug-in", identifier: "headerCell"),
        MenuItem(name: "Extension".localized, identifier: "menuCell", controller: "SimpleFilter", storyboard: "SimpleFilter"),
        
        // Network
        MenuItem(name: "Network", identifier: "headerCell"),
        MenuItem(name: "Encryption".localized, identifier: "menuCell", controller: "StreamEncryption", storyboard: "StreamEncryption"),
        MenuItem(name: "Precall Test".localized, identifier: "menuCell", controller: "PrecallTest", storyboard: "PrecallTest"),
    ]
    
    @IBOutlet weak var tableView:NSTableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    @IBAction func onClickSetting(_ sender: NSButton) {
        let selectedRow = tableView.selectedRow
        if (selectedRow >= 0) {
            tableView.deselectRow(selectedRow)
        }
        loadSplitViewItem(item: settings)
    }
    
    func loadSplitViewItem(item: MenuItem) {
        var storyboardName = ""
        
        if let name = item.storyboard {
            storyboardName = name
        } else {
            storyboardName = "Main"
        }
        let board: NSStoryboard = NSStoryboard(name: storyboardName, bundle: nil)
        
        guard let splitViewController = self.parent as? NSSplitViewController,
            let controllerIdentifier = item.controller,
            let viewController = board.instantiateController(withIdentifier: controllerIdentifier) as? BaseView else { return }
        
        let splititem = NSSplitViewItem(viewController: viewController as NSViewController)
        
        let detailItem = splitViewController.splitViewItems[1]
        if let detailViewController = detailItem.viewController as? BaseView {
            detailViewController.viewWillBeRemovedFromSplitView()
        }
        splitViewController.removeSplitViewItem(detailItem)
        splitViewController.addSplitViewItem(splititem)
    }
}

extension MenuController: NSTableViewDataSource, NSTableViewDelegate {
    func tableView(_ tableView: NSTableView, heightOfRow row: Int) -> CGFloat {
        let item = menus[row]
        return item.identifier == "menuCell" ? 32 : 18
    }
    
    func numberOfRows(in tableView: NSTableView) -> Int {
        return menus.count
    }
    
    func tableView(_ tableView: NSTableView, shouldSelectRow row: Int) -> Bool {
        let item = menus[row]
        return item.identifier != "headerCell"
    }
    
    func tableView(_ tableView: NSTableView, viewFor tableColumn: NSTableColumn?, row: Int) -> NSView? {
        let item = menus[row]
        // Get an existing cell with the MyView identifier if it exists
        let view = tableView.makeView(withIdentifier: NSUserInterfaceItemIdentifier(rawValue: item.identifier), owner: self) as? NSTableCellView

        view?.imageView?.image = nil
        view?.textField?.stringValue = item.name

        // Return the result
        return view;
    }
    
    func tableViewSelectionDidChange(_ notification: Notification) {
        if tableView.selectedRow >= 0 && tableView.selectedRow < menus.count {
            loadSplitViewItem(item: menus[tableView.selectedRow])
        }
    }
}

class ViewController: NSViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override var representedObject: Any? {
        didSet {
        // Update the view, if already loaded.
        }
    }
}

