package akka.co.drosteeffect.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import akka.co.drosteeffect.R;
import akka.co.drosteeffect.picchooser.SelectPictureActivity;
import akka.co.drosteeffect.utils.BitmapUtils;
import akka.co.drosteeffect.utils.FileUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by swb on 2017/2/28.
 */

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_PERMISSON_SORAGE = 1;
    public static final int REQUEST_PERMISSON_CAMERA = 2;

    public static final int SELECT_GALLERY_IMAGE_CODE = 7;
    public static final int TAKE_PHOTO_CODE = 8;
    public static final int ACTION_REQUEST_EDITIMAGE = 9;

    @BindView(R.id.img)
    ImageView mImg;
    @BindView(R.id.select_ablum)
    Button mSelectAblum;
    @BindView(R.id.take_photo)
    Button mTakePhoto;
    @BindView(R.id.edit_image)
    Button mEditImage;
    private int mImageWidth;
    private int mImageHeight;
    private String path;
    private Bitmap mainBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        //获取屏幕参数
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        mImageWidth = displayMetrics.widthPixels;
        mImageHeight = displayMetrics.heightPixels;


    }

    @OnClick({R.id.select_ablum, R.id.take_photo, R.id.edit_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_ablum:
                selectFromAblum();
                break;
            case R.id.take_photo:

                break;
            case R.id.edit_image:
                editImageClick();
                break;
        }
    }

    /**
     * 编辑图片
     */
    private void editImageClick() {
        String outputFile = FileUtils.getEidtFile() + System.currentTimeMillis() + ".jpg";
        EditImageActivity.start(this, path, outputFile, ACTION_REQUEST_EDITIMAGE);
    }

    /**
     * 从相册中选择图片
     */
    private void selectFromAblum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openAblumWithPermissionsCheck();
        } else {
            openAblum();
        }//end if
    }

    private void openAblum() {
        MainActivity.this.startActivityForResult(new Intent(
                        MainActivity.this, SelectPictureActivity.class),
                SELECT_GALLERY_IMAGE_CODE);
    }

    private void openAblumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSON_SORAGE);
            return;
        }
        openAblum();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSON_SORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openAblum();
            return;
        }//end if

        if (requestCode == REQUEST_PERMISSON_CAMERA
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doTakePhoto();
            return;
        }//end if
    }

    private void doTakePhoto() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // System.out.println("RESULT_OK");
            switch (requestCode) {
                case SELECT_GALLERY_IMAGE_CODE://
                    handleSelectFromAblum(data);
                    break;
                case TAKE_PHOTO_CODE://拍照返回

                    break;
                case ACTION_REQUEST_EDITIMAGE://
                    handleEditorImage(data);
                    break;
            }// end switch
        }
    }

    private void handleEditorImage(Intent data) {
        String newFilePath = data.getStringExtra(EditImageActivity.SAVE_FILE_PATH);
        boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IMAGE_IS_EDIT, false);

        if (isImageEdit)
            Toast.makeText(this, getString(R.string.save_path, newFilePath), Toast.LENGTH_LONG).show();
        //System.out.println("newFilePath---->" + newFilePath);
        Log.d("image is edit", isImageEdit + "");
        LoadImageTask loadTask = new LoadImageTask();
        loadTask.execute(newFilePath);
    }

    private void handleSelectFromAblum(Intent data) {
        String filepath = data.getStringExtra("imgPath");
        path = filepath;
        // System.out.println("path---->"+path);
        startLoadTask();
    }

    private void startLoadTask() {
        LoadImageTask task = new LoadImageTask();
        task.execute(path);
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapUtils.getSampledBitmap(params[0], mImageWidth / 4, mImageHeight / 4);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
            mImg.setImageBitmap(mainBitmap);
        }
    }// end inner class

}
