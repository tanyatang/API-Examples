package io.agora.api.example.examples.advanced;

import static io.agora.api.example.common.model.Examples.ADVANCED;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.agora.api.example.MainApplication;
import io.agora.api.example.R;
import io.agora.api.example.annotation.Example;
import io.agora.api.example.common.BaseFragment;
import io.agora.api.example.utils.CommonUtil;
import io.agora.api.example.utils.TokenUtils;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.EncodedVideoTrackOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.EncodedVideoFrameInfo;

@Example(
        index = 7,
        group = ADVANCED,
        name = R.string.item_pushexternal,
        actionId = R.id.action_mainFragment_to_PushExternalVideo,
        tipsId = R.string.pushexternalvideo
)
public class PushExternalVideoYUV extends BaseFragment implements View.OnClickListener {
    private static final String TAG = PushExternalVideoYUV.class.getSimpleName();

    private static final String[] fileUrls = new String[]{
            "https://download.agora.io/demo/test/cyclist_1920x1080_60fps_H264.mp4",
            "https://download.agora.io/demo/test/cyclist_1920x1080_60fps_HEVC.mp4",
//            "/assets/cyclist_1920x1080_60fps_H264.mp4",
//            "/assets/cyclist_1920x1080_60fps_HEVC.mp4",
    };

    private Button join;
    private EditText et_channel;
    private RtcEngineEx engine;
    private Spinner sp_push_files;
    private int myUid;
    private volatile boolean joined = false;

    private MediaExtractorThread mediaExtractorThread;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_push_externalvideo, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setKeepScreenOn(true);
        join = view.findViewById(R.id.btn_join);
        et_channel = view.findViewById(R.id.et_channel);
        view.findViewById(R.id.btn_join).setOnClickListener(this);
        sp_push_files = view.findViewById(R.id.sp_push_files);

        String[] fileNames = new String[fileUrls.length];
        for (int i = 0; i < fileUrls.length; i++) {
            String[] split = fileUrls[i].split("/");
            fileNames[i] = split[split.length - 1];
        }
        sp_push_files.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, fileNames));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Check if the context is valid
        Context context = getContext();
        if (context == null) {
            return;
        }
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            /**
             * The context of Android Activity
             */
            config.mContext = context.getApplicationContext();
            /**
             * The App ID issued to you by Agora. See <a href="https://docs.agora.io/en/Agora%20Platform/token#get-an-app-id"> How to get the App ID</a>
             */
            config.mAppId = getString(R.string.agora_app_id);
            /** Sets the channel profile of the Agora RtcEngine.
             CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile.
             Use this profile in one-on-one calls or group calls, where all users can talk freely.
             CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast
             channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams;
             an audience can only receive streams.*/
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            /**
             * IRtcEngineEventHandler is an abstract class providing default implementation.
             * The SDK uses this class to report to the app on SDK runtime events.
             */
            config.mEventHandler = iRtcEngineEventHandler;
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT);
            config.mAreaCode = ((MainApplication) getActivity().getApplication()).getGlobalSettings().getAreaCode();
            engine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace();
            getActivity().onBackPressed();
        }
    }


    @Override
    public void onDestroy() {
        if (mediaExtractorThread != null) {
            mediaExtractorThread.stop();
            mediaExtractorThread = null;
        }
        /**leaveChannel and Destroy the RtcEngine instance*/
        if (engine != null) {
            /**After joining a channel, the user must call the leaveChannel method to end the
             * call before joining another channel. This method returns 0 if the user leaves the
             * channel and releases all resources related to the call. This method call is
             * asynchronous, and the user has not exited the channel when the method call returns.
             * Once the user leaves the channel, the SDK triggers the onLeaveChannel callback.
             * A successful leaveChannel method call triggers the following callbacks:
             *      1:The local client: onLeaveChannel.
             *      2:The remote client: onUserOffline, if the user leaving the channel is in the
             *          Communication channel, or is a BROADCASTER in the Live Broadcast profile.
             * @returns 0: Success.
             *          < 0: Failure.
             * PS:
             *      1:If you call the destroy method immediately after calling the leaveChannel
             *          method, the leaveChannel process interrupts, and the SDK does not trigger
             *          the onLeaveChannel callback.
             *      2:If you call the leaveChannel method during CDN live streaming, the SDK
             *          triggers the removeInjectStreamUrl method.*/
            engine.leaveChannel();
            engine.stopPreview();
        }
        engine = null;
        super.onDestroy();
        handler.post(RtcEngine::destroy);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_join) {
            if (!joined) {
                CommonUtil.hideInputBoard(getActivity(), et_channel);
                // call when join button hit
                String channelId = et_channel.getText().toString();
                // Check permission
                if (AndPermission.hasPermissions(this, Permission.Group.STORAGE, Permission.Group.MICROPHONE, Permission.Group.CAMERA)) {
                    joinChannel(channelId);
                    return;
                }
                // Request permission
                AndPermission.with(this).runtime().permission(
                        Permission.Group.STORAGE,
                        Permission.Group.MICROPHONE,
                        Permission.Group.CAMERA
                ).onGranted(permissions ->
                {
                    // Permissions Granted
                    joinChannel(channelId);
                }).start();
            } else {
                joined = false;
                join.setText(getString(R.string.join));
                sp_push_files.setEnabled(true);
                if (mediaExtractorThread != null) {
                    mediaExtractorThread.stop();
                    mediaExtractorThread = null;
                }
                engine.leaveChannel();
                engine.stopPreview();
            }
        }
    }

    private void joinChannel(String channelId) {
        // Check if the context is valid
        Context context = getContext();
        if (context == null) {
            return;
        }

        /**Set up to play remote sound with receiver*/
        engine.setDefaultAudioRoutetoSpeakerphone(true);

        /**In the demo, the default is to enter as the anchor.*/
        engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        // Enables the video module.
        engine.enableVideo();

        /**Please configure accessToken in the string_config file.
         * A temporary token generated in Console. A temporary token is valid for 24 hours. For details, see
         *      https://docs.agora.io/en/Agora%20Platform/token?platform=All%20Platforms#get-a-temporary-token
         * A token generated at the server. This applies to scenarios with high-security requirements. For details, see
         *      https://docs.agora.io/en/cloud-recording/token_server_java?platform=Java*/
        TokenUtils.gen(requireContext(), channelId, 0, accessToken -> {
            publishVideoTrack(channelId, accessToken);
        });
    }

    private void publishVideoTrack(String channelId, String accessToken) {
        // Prevent repeated entry
        sp_push_files.setEnabled(false);
        join.setEnabled(false);

        String publishUrl = fileUrls[sp_push_files.getSelectedItemPosition()];
        if (mediaExtractorThread != null) {
            mediaExtractorThread.stop();
        }
        mediaExtractorThread = new MediaExtractorThread(requireContext(), publishUrl, new MediaExtractorCallback() {
            @Override
            public void onExtractFormat(MediaExtractorThread thread, String mine, int bitRate) {
                EncodedVideoTrackOptions encodedOpt = new EncodedVideoTrackOptions();
                encodedOpt.targetBitrate = bitRate / 1000;
                if (MediaFormat.MIMETYPE_VIDEO_HEVC.equalsIgnoreCase(mine)) {
                    encodedOpt.codecType = Constants.VIDEO_CODEC_H265;
                } else {
                    encodedOpt.codecType = Constants.VIDEO_CODEC_H264;
                }
                engine.setExternalVideoSource(true, false, Constants.ExternalVideoSourceType.ENCODED_VIDEO_FRAME, encodedOpt);

                ChannelMediaOptions option = new ChannelMediaOptions();
                option.autoSubscribeAudio = false;
                option.autoSubscribeVideo = false;
                option.publishEncodedVideoTrack = true;
                option.publishMicrophoneTrack = false;
                int res = engine.joinChannel(accessToken, channelId, 0, option);
                if (res != 0) {
                    // Usually happens with invalid parameters
                    // Error code description can be found at:
                    // en: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
                    // cn: https://docs.agora.io/cn/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
                    showAlert(RtcEngine.getErrorDescription(Math.abs(res)));
                    runOnUIThread(() -> {
                        sp_push_files.setEnabled(true);
                        join.setEnabled(true);
                    });
                    return;
                }
                thread.start();
            }

            @Override
            public void onExtractFrame(ByteBuffer buffer, long presentationTimeUs, int size,
                                       boolean isKeyFrame, int width, int height, int frameRate,
                                       String codecName) {
                EncodedVideoFrameInfo frameInfo = new EncodedVideoFrameInfo();
                if (MediaFormat.MIMETYPE_VIDEO_HEVC.equalsIgnoreCase(codecName)) {
                    frameInfo.codecType = Constants.VIDEO_CODEC_H265;
                } else {
                    frameInfo.codecType = Constants.VIDEO_CODEC_H264;
                }
                frameInfo.framesPerSecond = frameRate;
                frameInfo.frameType = isKeyFrame ? Constants.VIDEO_FRAME_TYPE_KEY_FRAME : Constants.VIDEO_FRAME_TYPE_DELTA_FRAME;
                int ret = engine.pushExternalEncodedVideoFrame(buffer, frameInfo);
                if (ret != Constants.ERR_OK) {
                    Log.e(TAG, "pushExternalEncodedVideoFrame error: " + ret);
                }
            }
        });
    }


    /**
     * IRtcEngineEventHandler is an abstract class providing default implementation.
     * The SDK uses this class to report to the app on SDK runtime events.
     */
    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {

        /**Occurs when a user leaves the channel.
         * @param stats With this callback, the application retrieves the channel information,
         *              such as the call duration and statistics.*/
        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            Log.i(TAG, String.format("local user %d leaveChannel!", myUid));
            showLongToast(String.format("local user %d leaveChannel!", myUid));
        }

        /**Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         * @param channel Channel name
         * @param uid User ID
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered*/
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            myUid = uid;
            joined = true;
            runOnUIThread(() -> {
                join.setEnabled(true);
                join.setText(getString(R.string.leave));
                sp_push_files.setEnabled(false);
            });
        }

        /**Occurs when a remote user (Communication)/host (Live Broadcast) joins the channel.
         * @param uid ID of the user whose audio state changes.
         * @param elapsed Time delay (ms) from the local user calling joinChannel/setClientRole
         *                until this callback is triggered.*/
        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid);
            showLongToast(String.format("user %d joined!", uid));
        }


        /**Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         * @param uid ID of the user whose audio state changes.
         * @param reason Reason why the user goes offline:
         *   USER_OFFLINE_QUIT(0): The user left the current channel.
         *   USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data
         *              packet was received within a certain period of time. If a user quits the
         *               call and the message is not passed to the SDK (due to an unreliable channel),
         *               the SDK assumes the user dropped offline.
         *   USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from
         *               the host to the audience.*/
        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason));
            showLongToast(String.format("user %d offline! reason:%d", uid, reason));
        }
    };


    private interface MediaExtractorCallback {

        void onExtractFormat(MediaExtractorThread thread, String codecName, int bitRate);

        void onExtractFrame(ByteBuffer buffer, long presentationTimeUs, int size,
                            boolean isKeyFrame, int width, int height, int frameRate,
                            String codecName);
    }

    private static final class MediaExtractorThread {
        private Thread thread;
        private MediaExtractor extractor;
        private final String url;
        private volatile boolean isExtracting = false;
        private volatile boolean isRunning = true;
        private MediaExtractorCallback callback;
        private Context context;

        private MediaExtractorThread(Context context, String url, MediaExtractorCallback callback) {
            this.context = context;
            this.url = url;
            this.callback = callback;
            thread = new Thread(this::extract);
            thread.start();
        }

        private void start() {
            isExtracting = true;
        }

        private void stop() {
            if (!isExtracting) {
                return;
            }
            isRunning = false;
            isExtracting = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        private void extract() {
            try {
                extractor = new MediaExtractor();
                if(url.startsWith("/assets") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    extractor.setDataSource(context.getAssets().openFd(url.substring(8)));
                }else{
                    extractor.setDataSource(url);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean hasVideoTrack = false;
            int maxByteCount = 1024 * 1024 * 2;
            int frameRate = 30;
            int width = 640;
            int height = 360;
            String mimeName = "";
            int bitRate = 0;
            byte[] sps = new byte[0];
            byte[] pps = new byte[0];
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video")) {
                    hasVideoTrack = true;
                    mimeName = mime;
                    extractor.selectTrack(i);
                    maxByteCount = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                        bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE);
                    }
                    width = format.getInteger(MediaFormat.KEY_WIDTH);
                    height = format.getInteger(MediaFormat.KEY_HEIGHT);
                    if(format.containsKey("csd-0")){
                        sps = format.getByteBuffer("csd-0").array();
                    }
                    if(format.containsKey("csd-1")){
                        pps = format.getByteBuffer("csd-1").array();
                    }
                    break;
                }
            }

            if (callback != null) {
                callback.onExtractFormat(this, mimeName, bitRate);
            }

            int frameInterval = 1000 / frameRate;
            ByteBuffer buffer = ByteBuffer.allocate(maxByteCount);
            while (isRunning && hasVideoTrack) {
                if (!isExtracting) {
                    continue;
                }
                long start = System.currentTimeMillis();
                int sampleSize = extractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }
                ByteBuffer outBuffer;
                boolean isKeyFrame = (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) > 0;
                byte[] bytes = new byte[sampleSize];
                buffer.get(bytes, 0, sampleSize);
                int outSize = sampleSize;
                if (isKeyFrame) {
                    outSize = sps.length + pps.length + sampleSize;
                    outBuffer = ByteBuffer.allocateDirect(outSize);
                    outBuffer.put(sps, 0, sps.length);
                    outBuffer.put(pps, 0, pps.length);
                } else {
                    outBuffer = ByteBuffer.allocateDirect(outSize);
                }
                outBuffer.put(bytes, 0, sampleSize);

                long timeUs = extractor.getSampleTime();
                if (callback != null) {
                    callback.onExtractFrame(outBuffer, timeUs, outSize, isKeyFrame, width, height, frameRate, mimeName);
                }
                long sleep = frameInterval - (System.currentTimeMillis() - start);
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (!extractor.advance()) {
                    // end of stream
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_NEXT_SYNC);
                }
            }

            extractor.release();
            extractor = null;

        }
    }
}
