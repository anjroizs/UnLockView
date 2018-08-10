package com.js.gesturedemo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyViewGroupB extends LinearLayout {
    public MyViewGroupB(Context context) {
        super(context);
    }
 
    public MyViewGroupB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i("MyViewGroupB","dispatchTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyViewGroupB","dispatchTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyViewGroupB","dispatchTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyViewGroupB", "dispatchTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.dispatchTouchEvent(ev);
        Log.i("MyViewGroupB","dispatchTouchEvent_RESULT="+result);
        return result;
    }
 
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i("MyViewGroupB","onInterceptTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyViewGroupB","onInterceptTouchEvent_ACTION_MOVE");
                return true;
            case MotionEvent.ACTION_UP:
                Log.i("MyViewGroupB","onInterceptTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyViewGroupB", "onInterceptTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.onInterceptTouchEvent(ev);
        Log.i("MyViewGroupB","onInterceptTouchEvent_RESULT="+result);
        return false;
    }
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i("MyViewGroupB","onTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyViewGroupB","onTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyViewGroupB","onTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyViewGroupB", "onTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.onTouchEvent(event);
        Log.i("MyViewGroupB","onTouchEvent_RESULT="+result);
        return result;
    }
}
