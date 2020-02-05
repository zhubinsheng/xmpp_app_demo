package com.android.king.intercom.discover;

import android.os.Handler;
import android.os.Message;

import com.android.king.intercom.job.JobHandler;
import com.android.king.intercom.network.Multicast;
import com.android.king.intercom.util.Command;
import com.android.king.intercom.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class SignInAndOutReq extends JobHandler {

    private String command;

    public SignInAndOutReq(Handler handler) {
        super(handler);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public void run() {
        if (command != null) {
            byte[] data = command.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(
                    data, data.length, Multicast.getMulticast().getInetAddress(), Constants.MULTI_BROADCAST_PORT);
            try {
                MulticastSocket multicastSocket = Multicast.getMulticast().getMulticastSocket();
                if (multicastSocket != null) {
                    multicastSocket.send(datagramPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (command.equals(Command.DISC_REQUEST)) {
                sendMsg2MainThread();
            } else if (command.equals(Command.DISC_LEAVE)) {
                setCommand(Command.DISC_REQUEST);
            }
        }
    }

    /**
     * 发送消息到主线程
     */
    private void sendMsg2MainThread() {
        Message message = new Message();
        message.what = AudioHandler.DISCOVERING_SEND;
        handler.sendMessage(message);
    }
}
