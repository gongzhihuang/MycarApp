package com.example.mycarapp;

/**
 * Created by Administrator on 2017/7/12.
 */

public final class BackgroundData {
    public static final float[] CANVAS_COORDS = new float[] {
            10, 5, 0,   // top right
            -10, 5, 0,  // top left
            -10, -5, 0, // bottom left
            10, -5 , 0,  // bottom right
            /*1,1,
            -1,1,
            -1,-1,
            1,-1,*/
      };
    public static final short[] VERTEX_INDEX =new short[] { 0, 1, 2, 2, 0, 3 };

    public static final float[] UV_TEX_VERTEX = new float[]{   // in clockwise order:
            1, 0,  // bottom right
            0, 0,  // bottom left
            0, 1,  // top left
            1, 1,  // top right
            /*1,0,
            0,0,
            0,1,
            1,1,*/

    };
}
