package akka.co.drosteeffect.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import akka.co.drosteeffect.contract.EditImageContract;
import akka.co.drosteeffect.model.EditImageModel;
import akka.co.drosteeffect.utils.BitmapUtils;
import akka.co.drosteeffect.utils.FileUtils;
import akka.co.drosteeffect.utils.GifMakeUtil;
import akka.co.drosteeffect.utils.Matrix3;
import akka.co.drosteeffect.view.widget.CropImageView;
import akka.co.drosteeffect.view.widget.imagezoom.ImageViewTouch;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by swb on 2017/3/2.
 */

public class EditImagePresenter implements EditImageContract.Presenter {
    private static final String TAG = "EditImagePresenter";

    private EditImageContract.View mView;
    private EditImageModel mModel;

    private Bitmap mainBitmap;// 底层显示Bitmap
    private int piece;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private String previewFile;

    private boolean hasPreview;

    public EditImagePresenter(Context context) {

        mView = (EditImageContract.View) context;
        mModel = new EditImageModel();
    }

    @Override
    public void saveCropImage(final Bitmap bitmap, final CropImageView cropPanel, final ImageViewTouch mainImage,
                              final String saveFilePath) {
        mView.showCropDialog();
        Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                RectF cropRect = cropPanel.getCropRect();// 剪切区域矩形
                Matrix touchMatrix = mainImage.getImageViewMatrix();
                float[] data = new float[9];
                touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
                Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
                Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
                Matrix m = new Matrix();
                m.setValues(inverseMatrix.getValues());
                m.mapRect(cropRect);// 变化剪切矩形

                Bitmap resultBit = composePhoto(bitmap, cropRect);
                //合成6次
                for (int i = 0; i < 6; i++) {
                    resultBit = composePhoto(resultBit, cropRect);
                }

                Rect innerframeImageRect = new Rect();
                cropRect.round(innerframeImageRect);
                Rect pictureframesImageRect = new Rect(0, 0, mainBitmap.getWidth(), mainBitmap.getHeight());

                cropPhoto(bitmap, resultBit, pictureframesImageRect, innerframeImageRect);

                BitmapUtils.saveBitmap(resultBit, saveFilePath);
                subscriber.onNext(resultBit);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mView.dismissCropDialog();
                        mView.loadCropImage(bitmap);
                    }
                });
    }

    @Override
    public void setEditData(Bitmap bit, int frame) {
        piece = frame;
        mainBitmap = bit;
    }

    @Override
    public void createGif(final int fps, final int width, final int height) {
        previewFile = "";
        hasPreview = false;
        final String filename = String.valueOf(System.currentTimeMillis());
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    previewFile = GifMakeUtil.createGifForBit(filename, bitmaps, fps, width, height);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e.getCause());
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        mView.dismissGifDialog();
                        hasPreview = true;
                        mView.finishGifMake(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hasPreview = false;
                        mView.dismissGifDialog();
                        mView.finishGifMake(false);
                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }

    @Override
    public void loadImage(final String filePath, final int imageWidth, final int imageHeight) {
        Observable.create(new Observable.OnSubscribe<Bitmap>() {

            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap sampledBitmap = BitmapUtils.getSampledBitmap(filePath, imageWidth,
                        imageHeight);
                subscriber.onNext(sampledBitmap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mView.loadMainImage(bitmap);
                    }
                });
    }

    @Override
    public boolean isHasPreView() {
        return hasPreview;
    }

    @Override
    public String getPreViewFile() {
        return previewFile;
    }

    @Override
    public int getGifBitmapSize() {
        return bitmaps.size();
    }


    /**
     * 裁剪图片
     *
     * @param pictureframesBit
     * @param innerframeBit
     * @param pictureframesImageRect
     * @param innerframeImageRect
     */
    public void cropPhoto(Bitmap pictureframesBit, Bitmap innerframeBit, Rect pictureframesImageRect, Rect
            innerframeImageRect) {

        ArrayList<Rect> pictureframes = new ArrayList<>();
        ArrayList<Rect> innerframe = new ArrayList<>();
        pictureframes.add(0, pictureframesImageRect);
        innerframe.add(0, innerframeImageRect);
        int mainImageWidth = mainBitmap.getWidth();
        int mainImageHeight = mainBitmap.getHeight();

        mModel.calculateFrame(pictureframes, innerframe, piece);
        /*for (int i = 0; i <= piece; i++) {
            Log.d(TAG, "cropPhoto: pictureframes-- " + i + "  ->" + pictureframes.get(i).left + "  " + pictureframes
                    .get(i).top +
                    "  " + pictureframes.get(i).right + "  " + pictureframes.get(i).bottom + "   width-->" +
                    pictureframes.get(i).width() + "   height-->" + pictureframes.get(i).height());
        }

        for (int i = 0; i <= piece; i++) {
            Log.d(TAG, "cropPhoto: innerframe-- " + i + "  ->" + innerframe.get(i).left + "  " + innerframe.get(i)
                    .top + "  " +
                    innerframe.get(i).right + "  " + innerframe.get(i).bottom + "   width-->" + innerframe.get(i)
                    .width() + "   height-->" + innerframe.get(i).height());
        }*/

        for (int i = 0; i < piece; i++) {
            Matrix matrix = new Matrix();
            float widthScale = (float) mainImageWidth / pictureframes.get(i).width();
            float heightScale = (float) mainImageHeight / pictureframes.get(i).height();
            //            Log.d(TAG, "cropPhoto: mainImgWidth-->" + mainImageWidth + "   mainImageHeight-->" +
            // mainImageHeight);
            //            Log.d(TAG, "cropPhoto: widthScale--->" + widthScale + "   heightScale---->" + heightScale);
            matrix.postScale(widthScale, heightScale);
            Rect pictureframesRect = pictureframes.get(i);
            Rect innerframeRect = innerframe.get(i);

            Bitmap resultBit = Bitmap.createBitmap(
                    pictureframesBit,
                    pictureframesRect.left,
                    pictureframesRect.top,
                    pictureframesRect.width(),
                    pictureframesRect.height(),
                    matrix, true);
            //Bitmap resultBitmap = Bitmap.createBitmap(resultBit).copy(Bitmap.Config.ARGB_8888, true);
            Bitmap composeBitmap = composePhoto(resultBit, innerframeBit, innerframeRect);
            bitmaps.add(composeBitmap);

            if (i < 10) {
                BitmapUtils.saveBitmap(composeBitmap, FileUtils.getCropFile() + "0" + i + "" + ".png");
            } else {
                BitmapUtils.saveBitmap(composeBitmap, FileUtils.getCropFile() + i + ".png");
            }
        }

    }

    /**
     * 合成图片
     *
     * @param resultBitmap
     * @param bitmap
     * @param cropRect
     * @return
     */
    public Bitmap composePhoto(Bitmap resultBitmap, Bitmap bitmap, Rect cropRect) {
        Bitmap resultBit = Bitmap.createBitmap(resultBitmap).copy(
                Bitmap.Config.ARGB_8888, true);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap).copy(
                Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);

        Rect src = new Rect(0, 0, resultBitmap.getWidth(), resultBitmap.getHeight());
        canvas.drawBitmap(bitmap1, src, cropRect, photoPaint);

        return resultBit;

    }

    /**
     * 合成图片
     *
     * @param bitmap
     * @param cropRect
     * @return
     */
    public Bitmap composePhoto(Bitmap bitmap, RectF cropRect) {
        Bitmap resultBit = Bitmap.createBitmap(bitmap).copy(
                Bitmap.Config.ARGB_8888, true);
        Bitmap innerBit = Bitmap.createBitmap(bitmap).copy(
                Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);

        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect dst = new Rect((int) (cropRect.left), (int) (cropRect.top), (int) (cropRect.right), (int) cropRect.bottom);
        canvas.drawBitmap(innerBit, src, dst, photoPaint);

        return resultBit;

    }
}
