package com.android.king.intercom.job;

import android.os.Handler;

/**
 * 数据处理节点
 *
 * @author yanghao1
 */
public abstract class JobHandler implements Runnable {

    protected Handler handler;

    public JobHandler(Handler handler) {
        this.handler = handler;
    }

    public void free() {

    }
}
