package com.wisesoft.mtcnn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private int minFaceSize = 30;
    private int testTimeCount = 1;
    private int threadsNumber = 8;

    private MTCNN mtcnn = new MTCNN();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        TextView tv = findViewById(R.id.sample_text);
        ImageView imageView = findViewById(R.id.imageView);

        //拷贝模型到/data/data/com./files/mtcnn 目录
        try {
            copyBigDataToSD("det1.bin");
            copyBigDataToSD("det2.bin");
            copyBigDataToSD("det3.bin");
            copyBigDataToSD("det1.param");
            copyBigDataToSD("det2.param");
            copyBigDataToSD("det3.param");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //模型初始化
        String modelPath = getFilesDir().getAbsolutePath()+ "/mtcnn/";
        mtcnn.FaceDetectionModelInit(modelPath);

        mtcnn.SetMinFaceSize(minFaceSize);
        mtcnn.SetTimeCount(testTimeCount);
        mtcnn.SetThreadsNumber(threadsNumber);

        Bitmap img = null;
        try {
            InputStream is = this.getAssets().open("t1.jpg");
            img = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap image = img.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(image);

        int width = image.getWidth();
        int height = image.getHeight();
        byte[] imageDate = getPixelsRGBA(image);

        long timeDetectFace = System.currentTimeMillis();
        int faceInfo[] = null;

        faceInfo = mtcnn.FaceDetect(imageDate, width, height, 4);

        timeDetectFace = System.currentTimeMillis() - timeDetectFace;
        Log.i("info", "人脸平均检测时间："+timeDetectFace/testTimeCount);

        if(faceInfo.length>1){
            int faceNum = faceInfo[0];
            tv.setText("图宽："+width+"高："+height+"人脸平均检测时间："+timeDetectFace/testTimeCount+" 数目：" + faceNum);
            Log.i("info", "图宽："+width+"高："+height+" 人脸数目：" + faceNum );

            Bitmap drawBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
            for(int i=0;i<faceNum;i++) {
                int left, top, right, bottom;
                Canvas canvas = new Canvas(drawBitmap);
                Paint paint = new Paint();
                left = faceInfo[1+14*i];
                top = faceInfo[2+14*i];
                right = faceInfo[3+14*i];
                bottom = faceInfo[4+14*i];
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);//不填充
                paint.setStrokeWidth(5);  //线的宽度
                canvas.drawRect(left, top, right, bottom, paint);
                //画特征点
                canvas.drawPoints(new float[]{faceInfo[5+14*i],faceInfo[10+14*i],
                        faceInfo[6+14*i],faceInfo[11+14*i],
                        faceInfo[7+14*i],faceInfo[12+14*i],
                        faceInfo[8+14*i],faceInfo[13+14*i],
                        faceInfo[9+14*i],faceInfo[14+14*i]}, paint);//画多个点
            }
            imageView.setImageBitmap(drawBitmap);
        }else{
            tv.setText("未检测到人脸");
        }


    }


    //提取像素点
    private byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        Log.i("info:", "start copy file " + strOutFileName);
        //File sdDir = Environment.getExternalStorageDirectory();//sd卡
        String sdDir = getFilesDir().getAbsolutePath(); // /data/data/com./files 目录
        File file = new File(sdDir.toString()+"/mtcnn/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString()+"/mtcnn/" + strOutFileName;
        Log.i("info",tmpFile);
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i("info:", "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(sdDir.toString()+"/mtcnn/"+ strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i("info:", "end copy file " + strOutFileName);

    }
}
