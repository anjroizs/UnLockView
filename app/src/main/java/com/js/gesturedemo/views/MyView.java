package com.js.gesturedemo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyView extends View {
    public MyView(Context context) {
        super(context);
    }
 
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i("MyView","dispatchTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyView","dispatchTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyView","dispatchTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyView", "dispatchTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.dispatchTouchEvent(event);
        Log.i("MyView","dispatchTouchEvent_RESULT="+result);
        return result;
    }
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i("MyView","onTouchEvent_ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyView","onTouchEvent_ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyView","onTouchEvent_ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("MyView", "onTouchEvent_ACTION_CANCEL");
                break;
        }
        boolean result = super.onTouchEvent(event);
        Log.i("MyView","onTouchEvent_RESULT="+result);
        return result;
    }
}
