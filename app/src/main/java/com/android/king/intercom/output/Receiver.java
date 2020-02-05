package com.android.king.intercom.output;

import android.os.Handler;
import android.os.Message;

import com.android.king.intercom.data.AudioData;
import com.android.king.intercom.data.MessageQueue;
import com.android.king.intercom.discover.AudioHandler;
import com.android.king.intercom.job.JobHandler;
import com.android.king.intercom.network.Multicast;
import com.android.king.intercom.util.Command;
import com.android.king.intercom.util.Constants;
import com.android.king.intercom.util.IPUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class Receiver extends JobHandler {

    public Receiver(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {
        while (true) {
            // 设置接收缓冲段
            byte[] receivedData = new byte[512];
            DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                // 接收数据报文
                MulticastSocket multicastSocket = Multicast.getMulticast().getMulticastSocket();
                if (multicastSocket != null) {
                    multicastSocket.receive(datagramPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 判断数据报文类型，并做相应处理
            if (datagramPacket.getLength() == Command.DISC_REQUEST.getBytes().length ||
                    datagramPacket.getLength() == Command.DISC_LEAVE.getBytes().length ||
                    datagramPacket.getLength() == Command.DISC_RESPONSE.getBytes().length) {
                handleCommandData(datagramPacket);
            } else {
                handleAudioData(datagramPacket);
            }
        }
    }

    /**
     * 处理命令数据
     *
     * @param packet 命令数据包
     */
    private void handleCommandData(DatagramPacket packet) {
        String content = new String(packet.getData()).trim();
        if (content.equals(Command.DISC_REQUEST) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            byte[] feedback = Command.DISC_RESPONSE.getBytes();
            // 发送数据
            DatagramPacket sendPacket = new DatagramPacket(feedback, feedback.length,
                    packet.getAddress(), Constants.MULTI_BROADCAST_PORT);
            try {
                MulticastSocket multicastSocket = Multicast.getMulticast().getMulticastSocket();
                if (multicastSocket != null) {
                    multicastSocket.send(sendPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 发送Handler消息
            sendMsg2MainThread(packet.getAddress().toString(), AudioHandler.DISCOVERING_RECEIVE);
        } else if (content.equals(Command.DISC_RESPONSE) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            // 发送Handler消息
            sendMsg2MainThread(packet.getAddress().toString(), AudioHandler.DISCOVERING_RECEIVE);
        } else if (content.equals(Command.DISC_LEAVE) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            sendMsg2MainThread(packet.getAddress().toString(), AudioHandler.DISCOVERING_LEAVE);
        }
    }

    /**
     * 处理音频数据
     *
     * @param packet 音频数据包
     */
    private void handleAudioData(DatagramPacket packet) {
        byte[] encodedData = Arrays.copyOf(packet.getData(), packet.getLength());
        AudioData audioData = new AudioData(encodedData);
        MessageQueue.getInstance(MessageQueue.DECODER_DATA_QUEUE).put(audioData);
    }

    /**
     * 发送Handler消息
     *
     * @param content 内容
     */
    private void sendMsg2MainThread(String content, int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = content;
        handler.sendMessage(msg);
    }

    @Override
    public void free() {
        Multicast.getMulticast().free();
    }
}
