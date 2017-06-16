package com.katori770.photowidget.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.katori770.photowidget.R;
import com.katori770.photowidget.manager.AppManager;
import com.katori770.photowidget.manager.GPhotoManager;


public class GooglePhotoFragment extends Fragment {
    private static final int REQUEST_GET_ACCOUNTS_PERMISSION = 9999;

    private GPhotoManager gPhotoManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (permissionsNotGranted()) return;
        initialize();
    }

    private boolean permissionsNotGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNTS_PERMISSION);
                return true;
            }
        }
        return false;
    }

    private void initialize() {
        gPhotoManager = AppManager.getInstance().getGooglePhotoManager();
        gPhotoManager.initialize(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_ACCOUNTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        gPhotoManager.onActivityResult(requestCode, resultCode, data);
    }
}
