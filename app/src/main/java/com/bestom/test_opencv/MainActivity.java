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
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;

    TextView choose_img,gray_img, gslb_img,tdh_img,gzlb_image, ezh_img,bcl_img, fs_img,pz_img,bj_img,jz_img,jq_img, decode_img;
    ImageView image_start,image_result;

    private Mat srcColor ;
    private Mat srcColorPreview ;
    private Mat srcGray ;
    private Mat srcGraydst ;
    private Mat imageGuussian ;
    private Mat imageSobel ;
    private Mat imageblur;
    private Mat imageThreshold;
    private Mat imageVar;
    private Mat kernel;
    private Mat element;

    private Rect rect;
    private RotatedRect minRotatedRect;

    public static final int CHOOSE_PHOTO = 2;

    private int n=1;

    private final String TAG=MainActivity.class.getSimpleName();

    private Bitmap dstbitmap;

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
        setContentView(R.layout.activity_main);

        init();
        initview();

    }

    private void init(){
        mContext=this;
        mActivity=this;

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        srcColor = new Mat();
        srcColorPreview = new Mat();
        srcGray = new Mat();
        srcGraydst = new Mat();
        imageGuussian=new Mat();
        imageSobel = new Mat();
        imageblur=new Mat();
        imageThreshold=new Mat();
        imageVar=new Mat();
        //设置内核形状和内核大小
        kernel= getStructuringElement(MORPH_RECT,new Size(2*n+1,2*n+1),new Point(n,n));
        element = getStructuringElement(0, new Size(10, 10));
    }

    private void initview(){
        choose_img=findViewById(R.id.choose_image);
        choose_img.setOnClickListener(this);
        gray_img=findViewById(R.id.gray_img);
        gray_img.setOnClickListener(this);
        gslb_img=findViewById(R.id.gslb_image);
        gslb_img.setOnClickListener(this);
        tdh_img=findViewById(R.id.tdh_image);
        tdh_img.setOnClickListener(this);
        gzlb_image=findViewById(R.id.gzlb_image);
        gzlb_image.setOnClickListener(this);
        ezh_img=findViewById(R.id.ezhcl_image);
        ezh_img.setOnClickListener(this);
        bcl_img=findViewById(R.id.bcl_image);
        bcl_img.setOnClickListener(this);
        pz_img=findViewById(R.id.pzcl_image);
        pz_img.setOnClickListener(this);
        fs_img=findViewById(R.id.fscl_image);
        fs_img.setOnClickListener(this);
        bj_img=findViewById(R.id.bjcl_image);
        bj_img.setOnClickListener(this);
        jz_img=findViewById(R.id.jzcl_image);
        jz_img.setOnClickListener(this);
        jq_img=findViewById(R.id.jqcl_image);
        jq_img.setOnClickListener(this);
        decode_img=findViewById(R.id.decodeqrcl_image);
        decode_img.setOnClickListener(this);

        image_start=findViewById(R.id.image_start);
        image_result=findViewById(R.id.image_result);
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
            image_start.setImageBitmap(bitmap);//ImageView显示图片
            dstbitmap=bitmap;
            Utils.bitmapToMat(bitmap,srcColor);
            //dstMat=new Mat(rgbMat.size(),rgbMat.type());
        }
        cursor.close();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.choose_image:
                Intent choose = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(choose,CHOOSE_PHOTO);//打开相册
                break;
            case R.id.gray_img:
                Imgproc.cvtColor(srcColor,srcGray,Imgproc.COLOR_BGR2GRAY);
                //CV_8UC1 为 8位无符号的单通道---灰度图片
                //CV_8UC3 为 8位无符号的三通道---RGB彩色图像
                //CV_8UC4 为 8位无符号的四通道---带透明色的RGB图像
//                rgbMat.convertTo(dstMat, CvType.CV_8UC1);
                Utils.matToBitmap(srcGray,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.gslb_image:
                //高斯滤波
                Imgproc.GaussianBlur(srcGray,imageGuussian,kernel.size(),0,0);
                Utils.matToBitmap(imageGuussian,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.tdh_image:
                //求得水平和垂直方向灰度图像的梯度和,使用Sobel算子
                Mat imageX16S =new Mat();
                Mat imageSobelX =new Mat();
                Mat imageY16S =new Mat();
                Mat imageSobelY=new Mat();
                //计算水平方向灰度梯度绝对值
                Imgproc.Sobel(imageGuussian, imageX16S, CV_16S, 1, 0, 3, 1, 0, 4);
                Core.convertScaleAbs(imageX16S, imageSobelX,1,0);
                //计算垂直方向灰度梯度绝对值
                Imgproc.Sobel(imageGuussian, imageY16S, CV_16S, 0, 1, 3, 1, 0, 4);
                Core.convertScaleAbs(imageY16S, imageSobelY,1,0);
                //合并梯度
                Core.addWeighted(imageSobelX,0.5,imageSobelY,0.5,0,imageSobel);

                Utils.matToBitmap(imageSobel,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.gzlb_image:
                //均值滤波
                Imgproc.blur(imageSobel,imageblur,kernel.size());
                Utils.matToBitmap(imageblur,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.ezhcl_image:
                //二值化处理
                Imgproc.threshold(imageblur,imageVar,100,255,THRESH_BINARY);
                Utils.matToBitmap(imageVar,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.bcl_image:
                //闭处理，填充条形码间隙
                Imgproc.morphologyEx(imageVar,imageVar, MORPH_CLOSE, element);
                Utils.matToBitmap(imageVar,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.fscl_image:
                //腐蚀处理
                Imgproc.erode(imageVar,imageVar,element);
                Utils.matToBitmap(imageVar,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.pzcl_image:
                //膨胀处理
                Imgproc.dilate(imageVar,imageVar,element);
                Utils.matToBitmap(imageVar,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.bjcl_image:
                // region边界处理
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hiera = new Mat();
                Imgproc.findContours(imageVar,contours,hiera,RETR_EXTERNAL,CHAIN_APPROX_SIMPLE);
                System.out.println("轮廓数量："+ contours.size());
                System.out.println("hierarchy类型："+ hiera);
                for (int i = 0; i<(contours.size()-1); i++)
                {
                    //region 获取最大边缘边界
                    if (contourArea(contours.get(i))>contourArea(contours.get(i+1)))
                        rect = Imgproc.boundingRect(contours.get(i));
                    else
                        rect =  Imgproc.boundingRect(contours.get(i+1));
                    //endregion

                    //region 获取最小边缘界限
                    if (contourArea(contours.get(i))>contourArea(contours.get(i+1)))
                        minRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
                    else
                        minRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i+1).toArray()));
                    //endregion

                }
        //endregion

                srcColorPreview=srcColor;
                //region 角点定位处理
                System.out.println("rect.tl"+ rect.tl().toString());
                System.out.println("rect.br"+ rect.br().toString());
                System.out.println("minRotatedRect.tl"+ minRotatedRect.boundingRect().tl().toString());
                System.out.println("minRotatedRect.br"+ minRotatedRect.boundingRect().br().toString());
                System.out.println("minRotatedRect.angle 角度"+ minRotatedRect.angle);
                //最大矩形外框
                Imgproc.rectangle(srcColorPreview, rect.tl(),rect.br(),new Scalar(2, 255, 2), 2);
                //最小矩形外框
                Imgproc.rectangle(srcColorPreview,minRotatedRect.boundingRect().tl(),minRotatedRect.boundingRect().br(),new Scalar(255,2,2),2);
                //endregion

                Utils.matToBitmap(srcColorPreview,dstbitmap);
                image_result.setImageBitmap(dstbitmap);

                break;
            case R.id.jqcl_image:
                Point[] rectPoint = new Point[4];
                minRotatedRect.points(rectPoint);

                int startLeft = (int)Math.abs(rectPoint[0].x);
                int startUp = (int)Math.abs(rectPoint[0].y < rectPoint[1].y ? rectPoint[0].y : rectPoint[1].y);
                int width = (int)Math.abs(rectPoint[2].x - rectPoint[0].x);
                int height = (int)Math.abs(rectPoint[1].y - rectPoint[0].y);

                System.out.println("startLeft = " + startLeft);
                System.out.println("startUp = " + startUp);
                System.out.println("width = " + width);
                System.out.println("height = " + height);

                for(Point p : rectPoint) {
                    System.out.println(p.x + " , " + p.y);
                }

                srcColorPreview = new Mat(srcColorPreview , new Rect(minRotatedRect.boundingRect().tl() ,minRotatedRect.boundingRect().br() ));
                dstbitmap=null;
                dstbitmap=Bitmap.createBitmap(srcColorPreview.cols(),srcColorPreview.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(srcColorPreview,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;
            case R.id.jzcl_image:
                // 获取矩形的四个顶点
                Point[] minPoint = new Point[4];
                minRotatedRect.points(minPoint);
                double angle = minRotatedRect.angle + 90;
                Point center = minRotatedRect.center;

                // 得到旋转矩阵算子
                Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 0.8);

                Imgproc.warpAffine(srcColorPreview, srcColorPreview, matrix, srcColorPreview.size(), 1, 0, new Scalar(0, 0, 0));
                dstbitmap=null;
                dstbitmap=Bitmap.createBitmap(srcColorPreview.cols(),srcColorPreview.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(srcColorPreview,dstbitmap);
                image_result.setImageBitmap(dstbitmap);
                break;

            case R.id.decodeqrcl_image:




                break;
            default:
                break;
        }
    }
}
