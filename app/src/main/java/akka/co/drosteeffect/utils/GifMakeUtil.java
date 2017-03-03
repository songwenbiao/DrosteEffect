package akka.co.drosteeffect.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bumptech.glide.gifencoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by swb on 2017/3/1.
 */
public class GifMakeUtil {

    public static String createGif(String filename, List<String> paths, int fps, int width, int height) throws IOException {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
            localAnimatedGifEncoder.start(baos);//start
            localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
            localAnimatedGifEncoder.setDelay(fps);
            if (paths.size() > 0) {
                for (int i = 0; i < paths.size(); i++) {
                    Bitmap bitmap = BitmapFactory.decodeFile(paths.get(i));
                    Bitmap resizeBm = BitmapUtils.resizeImage(bitmap, width, height);
                    localAnimatedGifEncoder.addFrame(resizeBm);
                }
            }
            localAnimatedGifEncoder.finish();//finish


            String path = FileUtils.getGifFile() + filename + ".gif";
            FileOutputStream fos = new FileOutputStream(path);
            baos.writeTo(fos);
            baos.flush();
            fos.flush();
            baos.close();
            fos.close();

        return path;
    }

    public static String createGifForBit(String filename, List<Bitmap> bitmaps, int fps, int width, int height) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
        localAnimatedGifEncoder.setDelay(fps);
        if (bitmaps.size() > 0) {
            for (int i = 0; i < bitmaps.size(); i++) {
                Bitmap bitmap = bitmaps.get(i);
                Bitmap resizeBm = BitmapUtils.resizeImage(bitmap, width, height);
                localAnimatedGifEncoder.addFrame(resizeBm);
            }
        }
        localAnimatedGifEncoder.finish();//finish
        String path = FileUtils.getGifFile() + filename + ".gif";
        FileOutputStream fos = new FileOutputStream(path);
        baos.writeTo(fos);
        baos.flush();
        fos.flush();
        baos.close();
        fos.close();

        return path;
    }
}
