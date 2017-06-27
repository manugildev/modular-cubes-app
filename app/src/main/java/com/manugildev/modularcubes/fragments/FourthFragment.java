package com.manugildev.modularcubes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class FourthFragment extends Fragment {

    private MainActivity activity;
    @BindView(R.id.webview)
    public WebView webView;
    @BindView(R.id.resetButton)
    public Button resetButton;

    public FourthFragment() {
    }

    public static FourthFragment newInstance() {
        FourthFragment fragment = new FourthFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fourth, container, false);
        ButterKnife.bind(this, rootView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
        webView.addJavascriptInterface(new WebAppInterface(activity), "android");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onAddedCube(long id) {
        ModularCube cube = activity.mData.get(id);
        if (cube != null) {
            System.out.println("javascript:addMember(" + id + "," + cube.getDepth() + "," + cube.getParent() + ")");
            webView.loadUrl("javascript:addMember(" + id + "," + cube.getDepth() + "," + cube.getParent() + ")");
        }
    }

    public void onCubeDeleted(long id) {
        System.out.println("javascript:deleteMember(" + id + ")");
        webView.loadUrl("javascript:deleteMember(" + id + ")");

    }

    @OnClick(R.id.resetButton)
    public void resetButton() {
        for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            ModularCube cube = entry.getValue();
            onCubeDeleted(cube.getDeviceId());
        }

        for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            ModularCube cube = entry.getValue();
            onAddedCube(cube.getDeviceId());
        }
    }

    public void onUpdatedCube(long id) {
        webView.loadUrl("javascript:rotate(" + id + ",1)");
    }

    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }
}
