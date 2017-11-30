package com.example.mycarapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder sfh;
    private Canvas canvas;
    private Paint p;
    private Bitmap mBitmap;
    private static int mScreenWidth;
    private static int mScreenHeight;
    private boolean isThreadRunning = true;
    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Constant.context = context;
        initialize();
        p = new Paint();
        p.setAntiAlias(true);
        sfh = this.getHolder();
        sfh.addCallback(this);
        this.setKeepScreenOn(true);
        setFocusable(true);
        this.getWidth();
        this.getHeight();

    }

    private void initialize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        this.setKeepScreenOn(true);// 保持屏幕常亮
    }

    class DrawVideo extends Thread {
        public DrawVideo() {
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public void run() {
            Paint pt = new Paint();
            pt.setAntiAlias(true);
            pt.setColor(Color.GREEN);
            pt.setTextSize(20);
            pt.setStrokeWidth(1);

            int bufSize = 512 * 1024; // 视频图片缓冲
            byte[] jpg_buf = new byte[bufSize]; // buffer to read jpg

            int readSize = 4096; // 每次最大获取的流4096
            byte[] buffer = new byte[readSize]; // buffer to read stream

            while (isThreadRunning) {

                URL url = null;
                HttpURLConnection urlConn = null;

                try {
                    url = new URL(ConnectINFO.VIDEO_URL);
                    urlConn = (HttpURLConnection) url.openConnection(); // 使用HTTPURLConnetion打开连接

                    int read = 0;
                    int status = 0;
                    int jpg_count = 0; // jpg数据下标

                    while (true) {
                        read = urlConn.getInputStream().read(buffer, 0, readSize);

                        if (read > 0) {
                            for (int i = 0; i < read; i++) {
                                switch (status) {
                                    // Content-Length:
                                    case 0:
                                        if (buffer[i] == (byte) 'C')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 1:
                                        if (buffer[i] == (byte) 'o')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 2:
                                        if (buffer[i] == (byte) 'n')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 3:
                                        if (buffer[i] == (byte) 't')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 4:
                                        if (buffer[i] == (byte) 'e')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 5:
                                        if (buffer[i] == (byte) 'n')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 6:
                                        if (buffer[i] == (byte) 't')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 7:
                                        if (buffer[i] == (byte) '-')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 8:
                                        if (buffer[i] == (byte) 'L')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 9:
                                        if (buffer[i] == (byte) 'e')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 10:
                                        if (buffer[i] == (byte) 'n')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 11:
                                        if (buffer[i] == (byte) 'g')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 12:
                                        if (buffer[i] == (byte) 't')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 13:
                                        if (buffer[i] == (byte) 'h')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 14:
                                        if (buffer[i] == (byte) ':')
                                            status++;
                                        else
                                            status = 0;
                                        break;
                                    case 15:
                                        if (buffer[i] == (byte) 0xFF)
                                            status++;
                                        jpg_count = 0;
                                        jpg_buf[jpg_count++] = (byte) buffer[i];
                                        break;
                                    case 16:
                                        if (buffer[i] == (byte) 0xD8) {
                                            status++;
                                            jpg_buf[jpg_count++] = (byte) buffer[i];
                                        } else {
                                            if (buffer[i] != (byte) 0xFF)
                                                status = 15;

                                        }
                                        break;
                                    case 17:
                                        jpg_buf[jpg_count++] = (byte) buffer[i];
                                        if (buffer[i] == (byte) 0xFF)
                                            status++;
                                        if (jpg_count >= bufSize)
                                            status = 0;
                                        break;
                                    case 18:
                                        jpg_buf[jpg_count++] = (byte) buffer[i];
                                        if (buffer[i] == (byte) 0xD9) {
                                            status = 0;
                                            // jpg接收完成
                                            // 显示图像
                                            {
                                                canvas = sfh.lockCanvas();
                                                canvas.drawColor(Color.BLACK);

                                                Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(jpg_buf));

                                                int width = mScreenWidth;
                                                int height = mScreenHeight;

                                                mBitmap = Bitmap
                                                        .createScaledBitmap(bmp,
                                                                width, height,
                                                                false);

                                                canvas.drawBitmap(
                                                        mBitmap,
                                                        (mScreenWidth - width) / 2,
                                                        (mScreenHeight - height) / 2,
                                                        null);

                                                sfh.unlockCanvasAndPost(canvas);// 画完一副图像，解锁画布
                                            }
                                        } else {
                                            if (buffer[i] != (byte) 0xFF)
                                                status = 17;
                                        }
                                        break;
                                    default:
                                        status = 0;
                                        break;

                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    urlConn.disconnect();
                    ex.printStackTrace();
                }
            }

        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isThreadRunning=false;
        try
        {
            Thread.sleep(300);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        isThreadRunning=true;
        new DrawVideo().start();
    }
}
