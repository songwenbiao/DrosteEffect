package akka.co.drosteeffect.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.felipecsl.gifimageview.library.GifImageView;

import akka.co.drosteeffect.R;
import akka.co.drosteeffect.contract.EditImageContract;
import akka.co.drosteeffect.presenter.EditImagePresenter;
import akka.co.drosteeffect.utils.FileUtils;
import akka.co.drosteeffect.view.widget.CropImageView;
import akka.co.drosteeffect.view.widget.imagezoom.ImageViewTouch;
import akka.co.drosteeffect.view.widget.imagezoom.ImageViewTouchBase;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by swb on 2017/2/28.
 */

public class EditImageActivity extends BaseActivity implements EditImageContract.View {
    private static final String TAG = "EditImageActivity";
    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";
    public static final String IMAGE_IS_EDIT = "image_is_edit";
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.apply)
    TextView applyBtn;
    @BindView(R.id.gifMake_btn)
    TextView gifMake;
    @BindView(R.id.banner_flipper)
    ViewFlipper mBannerFlipper;
    @BindView(R.id.bottom_gallery)
    TextView mBottomGallery;
    @BindView(R.id.crop_panel)
    CropImageView mCropPanel;
    @BindView(R.id.main_image)
    ImageViewTouch mMainImage;
    private EditImagePresenter mEditPresenter;

    public String filePath;// 需要编辑图片路径
    public String saveFilePath;// 生成的新图片路径
    private int imageWidth, imageHeight;// 展示图片控件 宽 高
    public Bitmap mainBitmap;// 底层显示Bitmap
    private Dialog mGifMakeLoading;
    private Dialog mCropImageLoading;
    private int piece = 17;

    /**
     * @param context
     * @param editImagePath
     * @param outputPath
     * @param requestCode
     */
    public static void start(Activity context, final String editImagePath, final String outputPath, final int
            requestCode) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        context.startActivityForResult(it, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        ButterKnife.bind(this);
        checkInitImageLoader();
        initView();
        initData();
    }

    private void initView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        mBannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
        mBannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
        mGifMakeLoading = BaseActivity.getLoadingDialog(EditImageActivity.this, R.string.gif_make,
                false);
        mCropImageLoading = BaseActivity.getLoadingDialog(EditImageActivity.this, R.string.saving_image,
                false);
    }

    public void initData() {
        mEditPresenter = new EditImagePresenter(this);
        filePath = getIntent().getStringExtra(FILE_PATH);
        saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);// 保存图片路径
        mEditPresenter.loadImage(filePath, imageWidth, imageHeight);
    }

    @Override
    public void showCropDialog() {
        mCropImageLoading.show();
    }

    @Override
    public void dismissCropDialog() {
        mCropImageLoading.dismiss();
    }

    @Override
    public void showGifDialog() {
        mGifMakeLoading.show();
    }

    @Override
    public void dismissGifDialog() {
        mGifMakeLoading.dismiss();
    }

    @Override
    public void loadMainImage(Bitmap bitmap) {
        if (mainBitmap != null) {
            mainBitmap.recycle();
            mainBitmap = null;
            System.gc();
        }
        mainBitmap = bitmap;
        mMainImage.setImageBitmap(bitmap);
        mMainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
    }

    @Override
    public void loadCropImage(Bitmap bitmap) {
        if (bitmap == null)
            return;

        if (mainBitmap != null
                && !mainBitmap.isRecycled()) {
            mainBitmap.recycle();
        }
        mainBitmap = bitmap;
        mMainImage.setImageBitmap(mainBitmap);
        mMainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        mCropPanel.setCropRect(mMainImage.getBitmapRect());
        backToMain();
    }

    @Override
    public void finishGifMake(boolean b) {
        if (b) {
            if (mEditPresenter.isHasPreView()) {
                View contentView = LayoutInflater.from(this).inflate(R.layout.layout_gif_preview, null);
                GifImageView gifView = (GifImageView) contentView.findViewById(R.id.gif_view);
                byte[] fileBytes = FileUtils.getFileBytes(mEditPresenter.getPreViewFile());
                if (fileBytes != null) {

                    gifView.setBytes(fileBytes);
                    gifView.startAnimation();
                }

                MaterialDialog mMaterialDialog = new MaterialDialog(this)
                        .setView(contentView)
                        .setCanceledOnTouchOutside(true);
                mMaterialDialog.show();
            }
        } else {
            Toast.makeText(this, "生成失败", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.back_btn, R.id.apply, R.id.gifMake_btn, R.id.bottom_gallery})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:

                break;
            case R.id.apply:
                mEditPresenter.saveCropImage(mainBitmap, mCropPanel, mMainImage, saveFilePath);
                break;
            case R.id.gifMake_btn:
                if (mEditPresenter.isHasPreView()) {
                    finishGifMake(true);

                } else {

                    if (mEditPresenter.getGifBitmapSize() > 1) {
                        mEditPresenter.createGif(58, mainBitmap.getWidth(), mainBitmap.getHeight());
                        showGifDialog();
                    } else {
                        Toast.makeText(EditImageActivity.this, "请添加图片", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bottom_gallery:
                mEditPresenter.setEditData(mainBitmap, piece);
                mCropPanel.setVisibility(View.VISIBLE);
                mMainImage.setImageBitmap(mainBitmap);
                mMainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

                mMainImage.setScaleEnabled(false);// 禁用缩放
                RectF r = mMainImage.getBitmapRect();
                mCropPanel.setRatioCropRect(r, 1f);
                // System.out.println(r.left + "    " + r.top);
                break;
        }
    }

    /**
     * 返回剪切前的状态
     */
    public void backToMain() {
        mCropPanel.setVisibility(View.GONE);
        mMainImage.setScaleEnabled(true);// 恢复缩放功能
        mCropPanel.setRatioCropRect(mMainImage.getBitmapRect(), -1);
        mBannerFlipper.showNext();
    }

}
