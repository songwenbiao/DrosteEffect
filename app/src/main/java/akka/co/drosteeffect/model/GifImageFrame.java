package akka.co.drosteeffect.model;

import android.graphics.Bitmap;

/**
 * Created by swb on 2017/3/1.
 */

public class GifImageFrame {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_ICON = 1;

    private String path;
    private int type;
    private Bitmap mBitmap;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBitmap(Bitmap bitmap){
        mBitmap = bitmap;
    }

    public Bitmap getBitmap(){
        return mBitmap;
    }

}
