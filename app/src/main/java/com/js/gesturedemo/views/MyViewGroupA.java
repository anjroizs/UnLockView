package com.js.gesturedemo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyViewGroupA extends LinearLayout {
    public MyViewGroupA(Context context) {
        super(context);
    }

    public MyViewGroupA(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("MyViewGroupA", "dispatchTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyViewGroupA", "dispatchTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyViewGroupA", "dispatchTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyViewGroupA", "dispatchTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.dispatchTouchEvent(ev);
        Log.i("MyViewGroupA", "dispatchTouchEvent_RESULT=" + result);
        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("MyViewGroupA", "onInterceptTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyViewGroupA", "onInterceptTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyViewGroupA", "onInterceptTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyViewGroupA", "onInterceptTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.onInterceptTouchEvent(ev);
        Log.i("MyViewGroupA", "onInterceptTouchEvent_RESULT=" + result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("MyViewGroupA", "onTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyViewGroupA", "onTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyViewGroupA", "onTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyViewGroupA", "onTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.onTouchEvent(event);
        Log.i("MyViewGroupA", "onTouchEvent_RESULT=" + result);
        return result;
    }
}
