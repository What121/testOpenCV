package com.bestom.test_opencv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestom.test_opencv.opencvUtils.DecodeUtils;
import com.bestom.test_opencv.opencvUtils.OpenCVUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;

    Button choose_img,cl_img,decode_img;
    ImageView value_img,result_img;
    TextView value_img_w,value_img_h,result_img_w,result_img_h,decode_result;

    private Mat baseMat1,baseMat2,userMat,resultMat;

    private Bitmap srcbitmap,resultbitmap;
    private int width,hight;

    private OpenCVUtils mOpenCVUtils;
    private DecodeUtils mDecodeUtils;

    private String result;
    public static final int CHOOSE_PHOTO = 2;
    private final String TAG=MainActivity.class.getSimpleName();

    private BaseLoaderCallback mLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //    surfaceView.setVisibility(View.VISIBLE);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        init();
        initview();
    }

    private void  init(){
        mContext=this;
        mActivity=this;

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        mOpenCVUtils=new OpenCVUtils();
        mDecodeUtils=new DecodeUtils();

        baseMat1 = new Mat();
        baseMat2=new Mat();
        userMat=new Mat();
        resultMat=new Mat();
        //设置内核形状和内核大小
//        kernel= getStructuringElement(MORPH_RECT,new Size(2*n+1,2*n+1),new Point(n,n));
//        element = getStructuringElement(0, new Size(10, 10));
    }

    private void  initview(){
        value_img=findViewById(R.id.image_start);
        result_img=findViewById(R.id.image_result);
        choose_img=findViewById(R.id.choose_image);
        choose_img.setOnClickListener(this);
        cl_img=findViewById(R.id.cl_img);
        cl_img.setOnClickListener(this);
        decode_img=findViewById(R.id.decode_bt);
        decode_img.setOnClickListener(this);

        value_img_w=findViewById(R.id.startimg_w);
        value_img_h=findViewById(R.id.startimg_h);
        result_img_w=findViewById(R.id.resultimg_w);
        result_img_h=findViewById(R.id.resultimg_h);
        decode_result=findViewById(R.id.result_code);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.choose_image:
                Intent choose = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(choose,CHOOSE_PHOTO);//打开相册
                break;
            case R.id.cl_img:
                RotatedRect minRotatedRect1,minRotatedRect2;
                //1.对原始图片做opencv基本处理
                userMat=baseMat1;
                userMat=mOpenCVUtils.gray(userMat);
                userMat=mOpenCVUtils.gslb(userMat);
                userMat=mOpenCVUtils.Sobel(userMat);
                userMat=mOpenCVUtils.jzlb(userMat);
                userMat=mOpenCVUtils.threshold(userMat);
                userMat=mOpenCVUtils.bcl(userMat);
                userMat=mOpenCVUtils.fscl(userMat);
                userMat=mOpenCVUtils.pzcl(userMat);
                //2.寻找边缘定位并 截取 得到mat
                minRotatedRect1=mOpenCVUtils.findContours(userMat);
                userMat=baseMat1;
                baseMat2=mOpenCVUtils.jqcl(userMat,minRotatedRect1);
                //3.对截取mat进行 open基本处理 + 边缘定位 校正
                userMat=baseMat2;
                userMat=mOpenCVUtils.gray(userMat);
                userMat=mOpenCVUtils.gslb(userMat);
                userMat=mOpenCVUtils.Sobel(userMat);
                userMat=mOpenCVUtils.jzlb(userMat);
                userMat=mOpenCVUtils.threshold(userMat);
                userMat=mOpenCVUtils.bcl(userMat);
                userMat=mOpenCVUtils.fscl(userMat);
                userMat=mOpenCVUtils.pzcl(userMat);

                minRotatedRect2=mOpenCVUtils.findContours(userMat);
                userMat=baseMat2;
                resultMat=mOpenCVUtils.jzcl(userMat,minRotatedRect2);

                //bitmap 显示imageview
                resultbitmap=Bitmap.createBitmap(resultMat.cols(),resultMat.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultMat,resultbitmap);
                result_img.setImageBitmap(resultbitmap);
                width=resultMat.rows();
                hight=resultMat.cols();
                result_img_w.setText("W:"+width);
                result_img_h.setText("H:"+hight);

                break;
            case R.id.decode_bt:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        result=null;
                        Mat redata=resultMat.clone();
                        //CvType.CV_8U 对应转换为 byte[]
                        redata.convertTo(redata, CvType.CV_8U);
                        redata=mOpenCVUtils.yuv(redata);
                        int size = (int) (redata.total() * redata.channels());
                        byte[] resultData = new byte[size];
                        redata.get(0,0,resultData);
                        result = mDecodeUtils.decode(resultData,width,hight);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result==null){
                                    decode_result.setText("空");
                                }else {
                                    decode_result.setText(result);
                                }
                            }
                        });
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri =data.getData();
        switch (requestCode){
            case CHOOSE_PHOTO:
                //选择图片
                choosephoto(uri);
                break;
            default:
                break;
        }
    }

    //图片选择器
    private void choosephoto(Uri uri){
        Uri selectedImage = uri;
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        //图片路径picturePath
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);//从路径加载出图片bitmap
        //bitmap = rotateBimap(this, -90, bitmap);//旋转图片-90°
        if (bitmap!=null){
            value_img.setImageBitmap(bitmap);//ImageView显示图片
            srcbitmap=bitmap;
            Utils.bitmapToMat(bitmap,baseMat1);
            int w=srcbitmap.getWidth();
            int h=srcbitmap.getHeight();
            value_img_w.setText("W:"+w);
            value_img_h.setText("H:"+h);
            //dstMat=new Mat(rgbMat.size(),rgbMat.type());
        }
        cursor.close();
    }
}
