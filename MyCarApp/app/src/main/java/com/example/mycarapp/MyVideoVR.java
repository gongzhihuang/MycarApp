package com.example.mycarapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import static com.example.mycarapp.BackgroundData.VERTEX_INDEX;

public class MyVideoVR extends GvrActivity implements GvrView.StereoRenderer {

    protected float[] modelCanvas = new float[16];
    protected float[] modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final float CAMERA_Z = 0.01f;
    private static final int COORDS_PER_VERTEX = 3;

    private FloatBuffer canvasVertices;
    private FloatBuffer mUvTexVertex;
    private ShortBuffer canvasVerticesINdex;

    private int canvasProgram;
    private int canvasPositionParam;
    private int canvasModelViewProjectionParam;

    private int mTexCoordHandle;
    private int mTexSamplerHandle;

    private float[] camera = new float[16];
    private float[] view = new float[16];
    private float[] headView = new float[16];
    private float[] modelViewProjection = new float[16];
    private float[] modelView = new float[16];
    private float[] headRotation = new float[4];;

    //private static final float MAX_MODEL_DISTANCE = 7.0f;
    private static final float MAX_MODEL_DISTANCE = 14.0f;

    private int TexName;

    Bitmap bitmap;
    public Handler mHandler;
    private Socket socket;
    OutputStream socketWriter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGvrView();
    }
    public void initializeGvrView() {
        setContentView(R.layout.myvideo_vr);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        //gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        //gvrView.setRenderer(this);
        gvrView.enableCardboardTriggerEmulation();
        gvrView.setAsyncReprojectionEnabled(false);
        setGvrView(gvrView);
        gvrView.setRenderer(this);

        new Thread() {
            public void run() {
                try {
                    socket = new Socket(InetAddress.getByName(ConnectINFO.IP),Integer.parseInt(ConnectINFO.PORT));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socketWriter = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                //对msg进行处理，获得其中的数据也就是计算结果
                //super.handleMessage(msg);
                bitmap =  (Bitmap) msg.obj;
            }
        };
        new DrawVideo().start();
    }

    @Override
    public void onPause() { super.onPause();}
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onRendererShutdown() {}
    @Override
    public void onSurfaceChanged(int width, int height) {}
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.d("GLSurfaceView", "surface created");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(BackgroundData.CANVAS_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        canvasVertices = bbVertices.asFloatBuffer();
        canvasVertices.put(BackgroundData.CANVAS_COORDS);
        canvasVertices.position(0);

        ByteBuffer mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2);
        mVertexIndexBuffer.order(ByteOrder.nativeOrder());
        canvasVerticesINdex= mVertexIndexBuffer.asShortBuffer();
        canvasVerticesINdex.put(VERTEX_INDEX);
        canvasVerticesINdex.position(0);

        ByteBuffer mUvTexVertexBuffer = ByteBuffer.allocateDirect(BackgroundData.UV_TEX_VERTEX.length * 4);
        mUvTexVertexBuffer.order(ByteOrder.nativeOrder());
        mUvTexVertex=mUvTexVertexBuffer.asFloatBuffer();
        mUvTexVertex.put(BackgroundData.UV_TEX_VERTEX);
        mUvTexVertex.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment);

        canvasProgram = GLES20.glCreateProgram();//创建一个空的OpenGL ES Program
        GLES20.glAttachShader(canvasProgram, vertexShader);//将vertex shader添加到program
        GLES20.glAttachShader(canvasProgram, passthroughShader);
        GLES20.glLinkProgram(canvasProgram);//创建可执行的 OpenGL ES program
        GLES20.glUseProgram(canvasProgram);//将program加入OpenGL ES环境中
        canvasPositionParam = GLES20.glGetAttribLocation(canvasProgram, "a_Position");//获取指向vertex shader的成员a_Position的 cubePositionParam
        canvasModelViewProjectionParam = GLES20.glGetUniformLocation(canvasProgram, "u_MVP");
        mTexCoordHandle = GLES20.glGetAttribLocation(canvasProgram, "a_texCoord");//
        mTexSamplerHandle = GLES20.glGetUniformLocation(canvasProgram, "s_texture");//

        updateModelPosition();
    }
    protected void updateModelPosition() {
        Matrix.setIdentityM(modelCanvas, 0);
        Matrix.translateM(modelCanvas, 0, modelPosition[0], modelPosition[1], modelPosition[2]);//偏移量 xyz平移量
    }
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);//相機視角，眼睛相對物體的位置改變，设置相机的位置(视口矩阵)
        //Matrix.setLookAtM（mVMatrix,0,//偏移量cx, cy, cz,//相机位置,tx, ty, tz,//观察点位置upx, upy, upz//顶部朝向）
        headTransform.getHeadView(headView, 0);//表示从相机到头部的变换的矩阵。头部原点被定义为两只眼睛之间的中心点,参数，headview要写入4x4列主要转换矩阵的数组
        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);//Provides the quaternion representing the head rotation.
        Log.d("GLSurfaceView", "onnewframe");
        GLES20.glUseProgram(canvasProgram);
        GLES20.glDeleteTextures(1, new int[] { TexName }, 0);
        teximage();
    }
    @Override
    public void onDrawEye(Eye eye) {
        Log.d("GLSurfaceView", "ondraweye");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);//计算投影和视口变换  getEyeView返回从相机转换为当前眼睛的矩阵
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);//返回这个眼睛的透视投影矩阵
        Matrix.multiplyMM(modelView, 0, view, 0, modelCanvas, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        //teximage();
        draw();
    }
    public void draw(){
        GLES20.glVertexAttribPointer(canvasPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, canvasVertices);//准备圖形的坐标数据
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mUvTexVertex);
        GLES20.glUniformMatrix4fv(canvasModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glUniform1i(mTexSamplerHandle, 0);
        GLES20.glEnableVertexAttribArray(canvasPositionParam);//启用一个指向圖形的顶点数组的cubePositionParam
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length, GLES20.GL_UNSIGNED_SHORT, canvasVerticesINdex);
        Log.d("GLSurfaceView", "huatu");
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(canvasPositionParam);//禁用指向圖形的顶点数组对于 attribute 类型的变量，我们需要先 enable，再赋值，绘制完毕之后再 disable。我们可以通过 GLES20.glDrawArrays 或者 GLES20.glDrawElements 开始绘制。注意，执行完毕之后，GPU 就在显存中处理好帧数据了，但此时并没有更新到 surface 上，是 GLSurfaceView 会在调用 renderer.onDrawFrame 之后，调用 mEglHelper.swap()，来把显存的帧数据更新到 surface 上
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);//
        //GLES20.glDeleteTextures(1, new int[] { TexName }, 0);
    }
    public void teximage(){
        int[] mTexNames = new int[1];
        GLES20.glGenTextures(1, mTexNames, 0);
        TexName = mTexNames[0];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TexName);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        Log.d("GLSurfaceView", "teximage");
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }
        return shader;
    }
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 内容接收线程
     */
    public class DrawVideo extends Thread {
        public DrawVideo() {
        }

        public void run() {
            int bufSize = 512 * 1024; // 视频图片缓冲
            byte[] jpg_buf = new byte[bufSize]; // buffer to read jpg

            int readSize = 4096; // 每次最大获取的流4096
            byte[] buffer = new byte[readSize]; // buffer to read stream

            while (true) {

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
                                            // 线程间通信，发送图像
                                            {
                                                Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(jpg_buf));
                                                Message msg = Message.obtain();
                                                msg.obj = bmp;
                                                mHandler.sendMessage(msg);
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
}
