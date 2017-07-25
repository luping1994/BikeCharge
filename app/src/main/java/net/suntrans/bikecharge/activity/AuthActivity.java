package net.suntrans.bikecharge.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.utils.CropImageUtils;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.UiUtils;

import java.util.List;


/**
 * Created by Looney on 2017/6/3.
 */

public class AuthActivity extends BasedActivity implements View.OnClickListener {
    private static final int PICTURE_FRONT = 1;
    private static final int PICTURE_REAR = 2;
    private static final int PICTURE_BIKE = 3;
    private static final String TAG = "AuthActivity";

    private int currentSelected = PICTURE_FRONT;
    private String frontUri = null;
    private String rearUri = null;
    private String bikeUri = null;


    private ImageView picFront;
    private ImageView picRear;
    private ImageView picBike;
    private BottomSheetDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initView();
    }

    private void initView() {
        findViewById(R.id.chooseIdRront).setOnClickListener(this);
        findViewById(R.id.chooseIdRear).setOnClickListener(this);
        findViewById(R.id.chooBikePic).setOnClickListener(this);
        findViewById(R.id.commit).setOnClickListener(this);


        picFront = (ImageView) findViewById(R.id.picFront);
        picRear = (ImageView) findViewById(R.id.picRear);
        picBike = (ImageView) findViewById(R.id.picBike);
    }


    private final int CAPTURE_RESULT = 102;
    private static final int REQUEST_CODE_CHOOSE = 101;

    private void openButtomDialog() {
        if (null == dialog) {
            dialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_choose_pic, null, false);
            dialog.setContentView(view);
            view.findViewById(R.id.paizhao).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPermission();
                    dialog.dismiss();
                }
            });
            view.findViewById(R.id.tuku).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打开图库
//                    Intent intent = new Intent(
//                            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, REQUEST_CODE_CHOOSE);
//                    dialog.dismiss();
                    CropImageUtils.getInstance().openAlbum(AuthActivity.this);
                }
            });

            view.findViewById(R.id.canel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    private void startCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_RESULT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseIdRront:
                currentSelected = PICTURE_FRONT;
                openButtomDialog();
                break;
            case R.id.chooseIdRear:
                currentSelected = PICTURE_REAR;
                openButtomDialog();
                break;
            case R.id.chooBikePic:
                currentSelected = PICTURE_BIKE;
                openButtomDialog();
                break;
            case R.id.commit:
                upLoadTask();
                break;

        }
    }

//    @Override
//    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
//            Uri uri = data.getData();
//            LogUtil.i("Gallery", "uri=: " + uri);
//            if (uri == null) {
//                Bundle extras = data.getExtras();
//                Bitmap image = null;
//                if (extras != null) {
//                    //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
//                    image = extras.getParcelable("data");
//                }
//                // 判断存储卡是否可以用，可用进行存储
//                String state = Environment.getExternalStorageState();
//                if (state.equals(Environment.MEDIA_MOUNTED)) {
//                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//                    File tempFile = new File(path, System.currentTimeMillis() + ".jpg");
//                    FileOutputStream b = null;
//                    try {
//                        b = new FileOutputStream(tempFile);
//                        image.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            b.flush();
//                            b.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    uri = Uri.fromFile(tempFile);
//                    System.out.println("uri="+uri);
//                } else {
//                    UiUtils.showToast("没有储存卡");
//                    return;
//                }
//            }
//            getUri(uri);
//        }
//        if (requestCode == CAPTURE_RESULT && resultCode == RESULT_OK) {
//            Uri uri = data.getData();
//            LogUtil.i("CAPTURE", "uri=: " + uri);
//            if (uri == null) {
//                Bundle extras = data.getExtras();
//                Bitmap image = null;
//                if (extras != null) {
//                    //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
//                    image = extras.getParcelable("data");
//                }
//                // 判断存储卡是否可以用，可用进行存储
//                String state = Environment.getExternalStorageState();
//                if (state.equals(Environment.MEDIA_MOUNTED)) {
//                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//                    File tempFile = new File(path, System.currentTimeMillis() + ".jpg");
//                    FileOutputStream b = null;
//                    try {
//                        b = new FileOutputStream(tempFile);
//                        image.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            b.flush();
//                            b.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        uri = FileProvider.getUriForFile(getApplicationContext(), "net.suntrans.bikecharge.fileProvider", tempFile);
//                        getUri(uri);
//                    } else {
//                        uri = Uri.fromFile(tempFile);
//                        getUri(uri);
//                    }
//                } else {
//                    UiUtils.showToast("没有储存卡");
//                }
//            }
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropImageUtils.getInstance().onActivityResult(this, requestCode, resultCode, data, new CropImageUtils.OnResultListener() {
            @Override
            public void takePhotoFinish(String path) {
                UiUtils.showToast("照片存放在：" + path);
                //拍照回调，去裁剪
//                CropImageUtils.getInstance().cropPicture(AuthActivity.this, path);
            }

            @Override
            public void selectPictureFinish(String path) {
                UiUtils.showToast("打开图片：" + path);
                getUri(path);
                //相册回调，去裁剪
//                CropImageUtils.getInstance().cropPicture(AuthActivity.this, path);
            }

            @Override
            public void cropPictureFinish(String path) {
                getUri(path);
                UiUtils.showToast("裁剪保存在：" + path);
                //裁剪回调
//                Glide.with(AuthActivity.this)
//                        .load(path)
//                        .into();
            }
        });
    }

    private void getUri(String uri) {
        ImageView target = null;
        switch (currentSelected) {
            case PICTURE_FRONT:
                frontUri = uri;
                target = picFront;
                break;
            case PICTURE_REAR:
                rearUri = uri;
                target = picRear;
                break;
            case PICTURE_BIKE:
                bikeUri = uri;
                target = picBike;
                break;
        }
//        Glide.with(this)
//                .load(uri)
//                .centerCrop()
//                .into(target);
    }


    private static final int RC_CAMARE_PERM = 124;

    public void checkPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        CropImageUtils.getInstance().takePhoto(AuthActivity.this);

    }


    private void upLoadTask() {
        System.out.println(frontUri);
        System.out.println(rearUri);
        System.out.println(bikeUri.toString());
    }


}
