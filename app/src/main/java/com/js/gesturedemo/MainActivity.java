package com.js.gesturedemo;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.jongsung.unlock.UnLockView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements UnLockView.OnCreateLockListener, UnLockView.OnUnLockListener {
    private UnLockView unLockView;
    private TextView titleTView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unLockView = findViewById(R.id.unLockView);
        titleTView = findViewById(R.id.titleTView);
        unLockView.setOnCreateLockListener(this);
        unLockView.setOnUnLockListener(this);
        unLockView.setMinUnLockNodes(3);
        unLockView.setStateCreate();
        titleTView.setText("请创建密码");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("MainActivity", "dispatchTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MainActivity", "dispatchTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MainActivity", "dispatchTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MainActivity", "dispatchTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.dispatchTouchEvent(ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("MainActivity", "onTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MainActivity", "onTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MainActivity", "onTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MainActivity", "onTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.onTouchEvent(event);
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    public void onCreateLockSuccess(List<UnLockView.Node> nodes, String createdLink) {
        titleTView.setText("请再次输入相同密码");
    }

    @Override
    public void onCreateLockRepeatSuccess(List<UnLockView.Node> nodes, String createdLink) {
        titleTView.setText("密码创建成功，请解锁密码");
        unLockView.setStateUnlock(createdLink);
    }

    @Override
    public void onCreateLockFailed(List<UnLockView.Node> nodes, int error) {
        switch (error) {
            case UnLockView.OnCreateLockListener.ERROR_MIN_NODES:
                titleTView.setText("至少连接" + unLockView.getMinUnLockNodes() + "个点");
                break;
            case UnLockView.OnCreateLockListener.ERROR_MAX_NODES:
                titleTView.setText("超出范围");
                break;
            case UnLockView.OnCreateLockListener.ERROR_PASSWORD_UN_MATCHABLE:
                titleTView.setText("两次不一致");
                break;
        }
    }

    @Override
    public void onUnLockSuccess(List<UnLockView.Node> linkedNodes) {
        titleTView.setText("密码已解锁");
    }

    @Override
    public void onUnLockFailed(List<UnLockView.Node> linkedNodes, int error) {
        if (unLockView.unLockErrorExceed()) {
            titleTView.setText("连续错误，已锁定");
            unLockView.setStateClosed();
            return;
        }
        switch (error) {
            case UnLockView.OnUnLockListener.ERROR_MIN_NODES:
                titleTView.setText("至少连接四个点,还剩"+unLockView.getUnLockErrorLeftCount()+"次机会");
                break;
            case UnLockView.OnUnLockListener.ERROR_MAX_NODES:
                titleTView.setText("超出最大范围,还剩"+unLockView.getUnLockErrorLeftCount()+"次机会");
                break;
            case UnLockView.OnUnLockListener.ERROR_WRONG_PASSWORD:
                titleTView.setText("密码错误,还剩"+unLockView.getUnLockErrorLeftCount()+"次机会");
                break;
        }
    }
}
