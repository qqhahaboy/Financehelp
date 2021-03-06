package com.example.miracle.financehelp.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.miracle.financehelp.App;
import com.example.miracle.financehelp.AppConstant;
import com.example.miracle.financehelp.R;
import com.example.miracle.financehelp.activity.LoginActivity;
import com.example.miracle.financehelp.activity.UpdateNickNameActivity;
import com.example.miracle.financehelp.activity.UpdatePasswordActivity;
import com.example.miracle.financehelp.entity.AccountDao;
import com.example.miracle.financehelp.entity.IncomeTypeDao;
import com.example.miracle.financehelp.entity.OutcomeTypeDao;
import com.example.miracle.financehelp.entity.User;
import com.example.miracle.financehelp.utils.NetworkUtils;
import com.example.miracle.financehelp.utils.SPUtils;
import com.example.miracle.financehelp.widget.HeadImgDialog;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadBatchListener;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import de.hdodenhof.circleimageview.CircleImageView;

public class MineFragment extends Fragment {
    private static final int TAKEPHOTO = 492;
    private final int REQUEST_CODE_CAMERA = 1000;
    private final int REQUEST_CODE_CROP = 1002;
    private final int REQUEST_CODE_EDIT = 1003;
    private final int REQUEST_CODE_GALLERY = 1001;
    @Bind(R.id.headImg)
    de.hdodenhof.circleimageview.CircleImageView headImg;
    @Bind(R.id.loginTipsTv)
    TextView loginTipsTv;
    @Bind(R.id.headLayout)
    RelativeLayout headLayout;
    @Bind(R.id.updateNameLayout)
    RelativeLayout updateNameLayout;
    @Bind(R.id.updatePassLayout)
    RelativeLayout updatePassLayout;
    @Bind(R.id.backupLayout)
    RelativeLayout backupLayout;
    @Bind(R.id.cleanLayout)
    RelativeLayout cleanLayout;
    @Bind(R.id.exitLayout)
    RelativeLayout exitLayout;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private AccountDao accountDao;
    private IncomeTypeDao incomeTypeDao;
    private OutcomeTypeDao outcomeTypeDao;


    /**
     * 上传头像
     *
     * @param photoPath
     */
    private void loadFile(String photoPath) {
        this.progressDialog.setTitle("上传头像中");
        this.progressDialog.setMessage("请稍后...");
        this.progressDialog.show();
        BmobFile.uploadBatch(new String[]{photoPath}, new UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> list, final List<String> urls) {
                if (list.size() == urls.size()) {
                    final User user = new User();
                    user.setHeadImgUrl(urls.get(0));
                    user.update(SPUtils.getString(AppConstant.OBJECTID), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            Snackbar.make(MineFragment.this.getView(), "上传成功", -1).show();
                            SPUtils.putString(AppConstant.HEADIMGURL, urls.get(0));
                            Glide.with(getActivity()).load(urls.get(0)).into(headImg);
                            progressDialog.dismiss();
                        }
                    });
                }
                Snackbar.make(MineFragment.this.getView(), "上传失败", -1).show();
            }

            @Override
            public void onProgress(int i, int i1, int i2, int i3) {

            }

            @Override
            public void onError(int i, String s) {
                Snackbar.make(MineFragment.this.getView(), "上传失败", -1).show();
            }
        });


    }


    @Nullable
    public View onCreateView(LayoutInflater paramLayoutInflater, @Nullable ViewGroup container, @Nullable Bundle paramBundle) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine, container, false);
        ButterKnife.bind(this, view);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        SPUtils.getInstance(getActivity());
        accountDao = App.getDaoSession().getAccountDao();
        incomeTypeDao = App.getDaoSession().getIncomeTypeDao();
        outcomeTypeDao = App.getDaoSession().getOutcomeTypeDao();
        return view;
    }


    public void onResume() {
        super.onResume();
        SPUtils.getInstance(getContext());
        if (SPUtils.getBoolean(AppConstant.ISLOAD)) {
            exitLayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(SPUtils.getString(AppConstant.NICKNAME))) {
                loginTipsTv.setText(SPUtils.getString(AppConstant.NICKNAME));
            }
            if (!TextUtils.isEmpty(SPUtils.getString(AppConstant.HEADIMGURL))) {
                Glide.with(getActivity()).load(SPUtils.getString(AppConstant.HEADIMGURL)).into(headImg);
            }
        } else {
            exitLayout.setVisibility(View.GONE);
            loginTipsTv.setText("请登录");
        }
    }


    @OnClick({R.id.headImg, R.id.headLayout, R.id.updateNameLayout, R.id.updatePassLayout, R.id.backupLayout, R.id.cleanLayout, R.id.exitLayout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.headLayout:
                if (SPUtils.getBoolean(AppConstant.ISLOAD)) {
                    final HeadImgDialog dialog = new HeadImgDialog(getActivity());
                    dialog.setTitle("选择头像的方式");
                    dialog.setOnItemClickListner(new HeadImgDialog.OnCLickListener() {
                        public void onCameraPhotoClick(View paramAnonymousView) {
                            GalleryFinal.openCamera(1000, new GalleryFinal.OnHanlderResultCallback() {
                                @Override
                                public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
                                    loadFile((resultList.get(0)).getPhotoPath());
                                }

                                @Override
                                public void onHanlderFailure(int requestCode, String errorMsg) {

                                }
                            });
                            dialog.dismiss();
                        }

                        public void onGalleryPhotoClick(View paramAnonymousView) {
                            GalleryFinal.openGallerySingle(1001, new GalleryFinal.OnHanlderResultCallback() {
                                @Override
                                public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
                                    loadFile((resultList.get(0)).getPhotoPath());

                                }

                                @Override
                                public void onHanlderFailure(int requestCode, String errorMsg) {

                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
                break;
            case R.id.updateNameLayout:
                Intent updateNameIntent;
                if (SPUtils.getBoolean(AppConstant.ISLOAD, false)) {
                    updateNameIntent = new Intent(getActivity(), UpdateNickNameActivity.class);
                } else {
                    updateNameIntent = new Intent(getActivity(), LoginActivity.class);
                }
                startActivity(updateNameIntent);
                break;
            case R.id.updatePassLayout:
                Intent updatePassIntent;
                if (SPUtils.getBoolean(AppConstant.ISLOAD, false)) {
                    updatePassIntent = new Intent(getActivity(), UpdatePasswordActivity.class);
                } else {
                    updatePassIntent = new Intent(getActivity(), LoginActivity.class);
                }
                startActivity(updatePassIntent);
                break;
            case R.id.backupLayout:

                if (SPUtils.getBoolean(AppConstant.ISLOAD, false)) {
                    if (accountDao.loadAll().size() == 0) {
                        Snackbar.make(getView(), "还没记录数据", Snackbar.LENGTH_SHORT).show();
                    } else {
                        backupDatabase();
                    }
                } else {
                    Intent backupIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(backupIntent);
                }

                break;
            case R.id.cleanLayout:
                cleanUpData();
                break;
            case R.id.exitLayout:
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("确认要退出")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SPUtils.putBoolean(AppConstant.ISLOAD, false);
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                dialogInterface.dismiss();
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                break;
        }

    }

    private void cleanUpData() {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("确认要清空数据吗")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        accountDao.deleteAll();
                        incomeTypeDao.deleteAll();
                        outcomeTypeDao.deleteAll();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        dialog.show();

    }

    /**
     * 备份数据
     */
    private void backupDatabase() {
        this.progressDialog.show();
        String str = getContext().getDatabasePath("test.db").getAbsolutePath();
        BmobFile.uploadBatch(new String[]{str}, new UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> list, List<String> urls) {
                if (list.size() == urls.size()) {
                    User user = new User();
                    user.setDatabaseUrl(urls.get(0));
                    user.update(SPUtils.getString(AppConstant.OBJECTID), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            Snackbar.make(getView(), "备份成功", Snackbar.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onProgress(int i, int i1, int i2, int i3) {
            }

            @Override
            public void onError(int i, String s) {
                Snackbar.make(getView(), "备份失败", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
