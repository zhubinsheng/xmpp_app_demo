package com.android.king.intercom.input;

import android.os.Handler;
import android.util.Log;

import com.android.king.SenderListener;
import com.android.king.intercom.data.AudioData;
import com.android.king.intercom.data.MessageQueue;
import com.android.king.intercom.job.JobHandler;
import com.android.king.intercom.network.Multicast;
import com.android.king.intercom.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * Socket发送
 *
 * @author yanghao1
 */
public class Sender extends JobHandler {

    private SenderListener senderListener;

    public Sender(Handler handler) {
        super(handler);
    }

    public Sender(Handler handler,SenderListener senderListener) {
        super(handler);
        this.senderListener = senderListener;
    }
    @Override
    public void run() {
        AudioData audioData;
        while ((audioData = MessageQueue.getInstance(MessageQueue.SENDER_DATA_QUEUE).take()) != null) {

            senderListener.onBackClick(audioData.getEncodedData());
            DatagramPacket datagramPacket = new DatagramPacket(
                    audioData.getEncodedData(), audioData.getEncodedData().length,
                    Multicast.getMulticast().getInetAddress(), Constants.MULTI_BROADCAST_PORT);
            Log.i("zbs", "发送消息");
//            try {
//                MulticastSocket multicastSocket = Multicast.getMulticast().getMulticastSocket();
//                if (multicastSocket != null) {
//                    multicastSocket.send(datagramPacket);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void free() {
        Multicast.getMulticast().free();
    }
}
