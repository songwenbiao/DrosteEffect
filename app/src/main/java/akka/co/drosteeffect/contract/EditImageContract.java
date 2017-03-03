package akka.co.drosteeffect.contract;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;

import akka.co.drosteeffect.view.widget.CropImageView;
import akka.co.drosteeffect.view.widget.imagezoom.ImageViewTouch;

/**
 * Created by swb on 2017/3/2.
 */

public interface EditImageContract {
    interface Model {
        /**
         * 计算大矩形裁剪的位置， 小矩形在裁剪并放大的大矩形中的位置
         * @param pictureframes
         * @param innerframe
         * @param piece
         */
        void calculateFrame(ArrayList<Rect> pictureframes, ArrayList<Rect> innerframe, int piece);
    }

    interface View {

        void showCropDialog();

        void dismissCropDialog();

        void showGifDialog();

        void dismissGifDialog();

        /**
         * 加载需要裁剪的图片
         * @param bitmap
         */
        void loadMainImage(Bitmap bitmap);

        /**
         * 加载裁剪后的图片
         * @param bitmap
         */
        void loadCropImage(Bitmap bitmap);

        /**
         * 完成gif创建
         * @param b
         */
        void finishGifMake(boolean b);
    }

    interface Presenter {

        /**
         * 保存裁剪的图片
         * @param bitmap
         * @param cropPanel
         * @param mainImage
         * @param saveFilePath
         */
        void saveCropImage(Bitmap bitmap, CropImageView cropPanel, ImageViewTouch mainImage, String saveFilePath);

        /**
         * 设置需要编辑的数据
         * @param bit 需要编辑的bitmap
         * @param frame gif帧数
         */
        void setEditData(Bitmap bit, int frame);

        /**
         * 开始合成gif
         * @param fps 每帧播放的时间
         * @param width gif width
         * @param height gif height
         */
        void createGif(int fps, int width, int height);

        /**
         * 加载图片
         * @param filePath
         * @param imageWidth
         * @param imageHeight
         */
        void loadImage(String filePath, int imageWidth, int imageHeight);

        /**
         * 是否有预览gif
         * @return
         */
        boolean isHasPreView();

        /**
         * 获取gif 路径
         * @return
         */
        String getPreViewFile();

        /**
         * 获取合成gif的bitmap list size
         * @return
         */
        int getGifBitmapSize();

    }
}
