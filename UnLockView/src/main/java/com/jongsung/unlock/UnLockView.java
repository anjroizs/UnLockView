package com.jongsung.unlock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JongSung
 * @Description 密码锁view；包括
 * 创建密码{@link #setStateCreate()};
 * 解锁密码{@link #setStateUnlock(String)};
 * 修改密码(先解锁{@link #setStateUnlock(String)}后创建锁{@link #setStateCreate()})
 * @date 2018/7/26 0026
 */
public class UnLockView extends View {
    private Node[] nodes;
    private Path linkingPath, linkedPath;//正在连接的手势路径,已连接完整的路径
    private Paint gesturePaint;//路径画笔
    private int minUnLockNodes = 4;//最小解锁节点
    private int maxUnLockErrorCount = 5;//最多错误次数
    private int maxUnLockNodes = Node.NODE_COUNT;//最大解锁节点
    private int gesturePaintNormalColor = 0xff00ff00;
    private int gesturePaintErrorColor = 0xffff0000;
    private int unLockErrorCount = 0;
    /**
     * 节点最小高度
     */
    private int NODE_MIN_HEIGHT = 100;
    /**
     * View高度不确定时，节点垂直间距
     */
    private int NODE_WRAP_VERTICAL_SPACING = 100;

    /**
     * 当前View的功能：创建手势密码 or 解锁密码
     */
    public static final int STATE_UNKNOWN = -1;//
    /**
     * 创建手势密码
     */
    public static final int STATE_CREATE = 0;
    /**
     * 解锁密码
     */
    public static final int STATE_UNLOCK = 1;
    private int state = STATE_UNKNOWN;

    /**
     * 节点的展示状态：未连接状态
     */
    public static final int NODE_NORMAL = 0;
    /**
     * 节点的展示状态：连接状态
     */
    public static final int NODE_LINKED = 1;
    /**
     * 节点的展示状态：连接错误状态
     */
    public static final int NODE_LINKED_ERROR = 2;
    private int nodeStatus = NODE_NORMAL;


    private static int disappearTime = 0;


    private String matchableLink;

    public UnLockView(Context context) {
        super(context);
        init(context);
    }

    public UnLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UnLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        linkingPath = new Path();
        linkedPath = new Path();
        gesturePaint = new Paint();
        gesturePaint.setStyle(Paint.Style.STROKE);
        gesturePaint.setStrokeWidth(3);
        gesturePaint.setAntiAlias(true);
        gesturePaint.setDither(true);
        gesturePaint.setStrokeJoin(Paint.Join.ROUND);
        gesturePaint.setStrokeCap(Paint.Cap.ROUND);
        gesturePaint.setColor(gesturePaintNormalColor);

        nodes = new Node[Node.NODE_COUNT];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new DefaultNode(i);
        }
    }

    /**
     * 开始创建手势密码
     */
    public void setStateCreate() {
        setState(STATE_CREATE);
    }

    /**
     * 解锁手势密码
     *
     * @param passwordLink 需要解锁的密码流
     */
    public void setStateUnlock(String passwordLink) {
        if (TextUtils.isEmpty(passwordLink)) {
            return;
        }
        setState(STATE_UNLOCK);
        this.matchableLink = passwordLink;
    }

    /**
     * 关闭手势密码
     */
    public void setStateClosed() {
        setState(STATE_UNKNOWN);
    }

    public int getMaxUnLockErrorCount() {
        return maxUnLockErrorCount;
    }

    public void setMaxUnLockErrorCount(int maxUnLockErrorCount) {
        this.maxUnLockErrorCount = maxUnLockErrorCount;
    }

    public int getUnLockErrorCount() {
        return unLockErrorCount;
    }

    public int getUnLockErrorLeftCount() {
        return maxUnLockErrorCount - unLockErrorCount;
    }

    public boolean unLockErrorExceed() {
        return unLockErrorCount >= maxUnLockErrorCount;
    }

    /**
     * 设置密码锁状态{@link #STATE_CREATE}{@link #STATE_UNLOCK}
     *
     * @param state
     */
    private void setState(int state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        unLockErrorCount = 0;
        resetAll();
        nodeStatus = NODE_NORMAL;
        invalidate();
    }

    public void setCustomNodes(Node[] nodes) {
        this.nodes = nodes;
    }

    /**
     * 检测当前坐标是否经过了某个节点
     *
     * @param x
     * @param y
     * @return
     */
    private Node checkTouchOnNode(float x, float y) {
        if (nodes == null) {
            return null;
        }
        for (int i = 0; i < nodes.length; i++) {
            if (!nodes[i].isLinked() && nodes[i].contains(x, y)) {
                return nodes[i];
            }
        }
        return null;
    }

    private void resetAll() {
        initNode = null;
        nodeStatus = NODE_NORMAL;
        lastLinkedNodeX = 0;
        lastLinkedNodeY = 0;
        linkedNodes.clear();
        linkedPath.reset();
        linkingPath.reset();
        gesturePaint.setColor(gesturePaintNormalColor);
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setLinked(false);
            }
        }
    }

    /**
     * 当前事件下最后一个被连接的节点坐标
     */
    private float lastLinkedNodeX, lastLinkedNodeY;
    /**
     * 一次完整触摸事件的初始节点
     */
    private Node initNode;
    /**
     * 已连接的节点
     */
    private ArrayList<Node> linkedNodes = new ArrayList<>(Node.NODE_COUNT);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (state == STATE_UNKNOWN) {
            return super.onTouchEvent(event);
        }
        removeCallbacks(delayShowRunnable);
        final float x = event.getX();
        final float y = event.getY();
        gesturePaint.setColor(gesturePaintNormalColor);
        final int actionMasked = event.getAction() & MotionEvent.ACTION_MASK;
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            resetAll();
        }
        //当前事件是否触发了某个节点
        final Node currentNode = checkTouchOnNode(x, y);
        if (currentNode != null) {
            linkedNodes.add(currentNode);
            currentNode.setLinked(true);
        }
        disappearTime = 0;
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                if (currentNode != null) {
                    this.initNode = currentNode;
                    lastLinkedNodeX = initNode.getPointX();
                    lastLinkedNodeY = initNode.getPointY();
                    linkingPath.reset();
                    linkingPath.moveTo(lastLinkedNodeX, lastLinkedNodeY);
                    linkingPath.lineTo(currentNode.pointX, currentNode.pointY);
                    //每连接好一个点都需要记录下已连接的路径
                    linkedPath.moveTo(lastLinkedNodeX, lastLinkedNodeY);
                    Log.e("UnLockView", "ACTION_DOWN lineTo==" + currentNode.pointX + "-" + currentNode.pointY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (initNode != null) {
                    if (currentNode == null) {
                        linkingPath.reset();
//                        linkingPath.addPath(linkedPath);
                        linkingPath.moveTo(lastLinkedNodeX, lastLinkedNodeY);
                        linkingPath.lineTo(x, y);
                    } else {
                        //移动过程中连接了新的节点
                        lastLinkedNodeX = currentNode.pointX;
                        lastLinkedNodeY = currentNode.pointY;
//                        //处理linking状态下path中间经过的节点
//                        dealTransientNode();
                        linkedPath.lineTo(lastLinkedNodeX, lastLinkedNodeY);
                        //绘制路径到当前手势位置
                        linkingPath.reset();
                        linkingPath.moveTo(lastLinkedNodeX, lastLinkedNodeY);
                        linkingPath.lineTo(x, y);
                    }
                } else {
                    if (currentNode != null) {
                        this.initNode = currentNode;
                        lastLinkedNodeX = currentNode.pointX;
                        lastLinkedNodeY = currentNode.pointY;
                        linkedPath.moveTo(lastLinkedNodeX, lastLinkedNodeY);
                        linkedPath.lineTo(lastLinkedNodeX, lastLinkedNodeY);
                        linkingPath.moveTo(lastLinkedNodeX, lastLinkedNodeY);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (initNode != null) {
                    linkingPath.reset();
                    if (state == STATE_CREATE) {
                        dealCreateLockResult();
                    } else if (state == STATE_UNLOCK) {
                        dealUnLockResult();
                    }
                }
                break;
        }
        invalidate();
        return true;
    }

    private void setResultError() {
        nodeStatus = NODE_LINKED_ERROR;
        disappearTime = 1000;
        gesturePaint.setColor(gesturePaintErrorColor);
    }


    private void setResultLinked() {
        nodeStatus = NODE_LINKED;
        disappearTime = 1000;
    }

    /**
     * 处理创建或修改密码锁的结果
     */
    private void dealCreateLockResult() {
        if (linkedNodes.size() < minUnLockNodes) {
            setResultError();
            if (onCreateLockListener != null) {
                onCreateLockListener.onCreateLockFailed(linkedNodes, OnCreateLockListener.ERROR_MIN_NODES);
            }
        } else if (linkedNodes.size() > maxUnLockNodes) {
            setResultError();
            if (onCreateLockListener != null) {
                onCreateLockListener.onCreateLockFailed(linkedNodes, OnCreateLockListener.ERROR_MAX_NODES);
            }
        } else {
            if (TextUtils.isEmpty(matchableLink)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < linkedNodes.size(); i++) {
                    sb.append(linkedNodes.get(i).index);
                }
                matchableLink = sb.toString();
                disappearTime = 1000;
                if (onCreateLockListener != null) {
                    onCreateLockListener.onCreateLockSuccess(linkedNodes, matchableLink);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < linkedNodes.size(); i++) {
                    sb.append(linkedNodes.get(i).index);
                }
                if (matchableLink.equals(sb.toString())) {
                    setResultLinked();
                    if (onCreateLockListener != null) {
                        onCreateLockListener.onCreateLockRepeatSuccess(linkedNodes, matchableLink);
                    }
                } else {
                    setResultError();
                    if (onCreateLockListener != null) {
                        onCreateLockListener.onCreateLockFailed(linkedNodes, OnCreateLockListener.ERROR_PASSWORD_UN_MATCHABLE);
                    }
                }
            }
        }
    }

    /**
     * 处理解锁结果
     */
    private void dealUnLockResult() {
        if (linkedNodes.size() < minUnLockNodes) {
            unLockErrorCount++;
            setResultError();
            if (onUnLockListener != null) {
                onUnLockListener.onUnLockFailed(linkedNodes, OnUnLockListener.ERROR_MIN_NODES);
            }
        } else if (linkedNodes.size() > maxUnLockNodes) {
            unLockErrorCount++;
            setResultError();
            if (onUnLockListener != null) {
                onUnLockListener.onUnLockFailed(linkedNodes, OnUnLockListener.ERROR_MAX_NODES);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < linkedNodes.size(); i++) {
                sb.append(linkedNodes.get(i).index);
            }
            if (matchableLink.equals(sb.toString())) {
                setResultLinked();
                if (onUnLockListener != null) {
                    onUnLockListener.onUnLockSuccess(linkedNodes);
                }
                setStateClosed();
            } else {
                unLockErrorCount++;
                setResultError();
                if (onUnLockListener != null) {
                    onUnLockListener.onUnLockFailed(linkedNodes, OnUnLockListener.ERROR_WRONG_PASSWORD);
                }
            }
        }
    }

    /**
     * 处理线段经过的节点
     */
    private void dealTransientNode() {
        if (linkedNodes.size() <= 1) {
            return;
        }
        Node node0 = linkedNodes.get(linkedNodes.size() - 2);
        Node node1 = linkedNodes.get(linkedNodes.size() - 1);
        final float x0 = node0.pointX;
        final float y0 = node0.pointY;
        final float x1 = node1.pointX;
        final float y1 = node1.pointY;
        Node node;
        //对于3X3矩阵来说，永远至多一个会被手势划线经过
        for (int i = 0; i < nodes.length; i++) {
            node = nodes[i];
            if (!node.isLinked() && node.contains(x0, y0, x1, y1)) {
                node.setLinked(true);
                linkedNodes.add(linkedNodes.size() - 1, nodes[i]);
                linkedPath.lineTo(node.pointX, node.pointY);
                linkingPath.moveTo(node.pointX, node.pointY);
                linkingPath.lineTo(node.pointX, node.pointY);
                break;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = (Node.NODE_RAW_COUNT - 1) * NODE_WRAP_VERTICAL_SPACING + Node.NODE_RAW_COUNT * NODE_MIN_HEIGHT + getPaddingTop() + getPaddingBottom();
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (nodes == null) {
            return;
        }
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childRight = getWidth() - getPaddingRight();
        final int childBottom = getHeight() - getPaddingBottom();
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].onLayout(changed, childLeft, childTop, childRight, childBottom, i);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (nodes == null) {
            return;
        }
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].draw(canvas, nodeStatus);
        }
        drawLine(canvas);
        if (disappearTime > 0) {
            resetAll();
            postDelayed(delayShowRunnable, disappearTime);
            disappearTime = 0;
        }
    }

    private Runnable delayShowRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private void drawLine(Canvas canvas) {
        canvas.drawPath(linkedPath, gesturePaint);
        canvas.drawPath(linkingPath, gesturePaint);
    }

    public void setMinUnLockNodes(int minUnLockNodes) {
        this.minUnLockNodes = minUnLockNodes;
    }

    public int getMinUnLockNodes() {
        return minUnLockNodes;
    }

    public void setMaxUnLockNodes(int maxUnLockNodes) {
        if (maxUnLockNodes > Node.NODE_COUNT) {
            this.maxUnLockNodes = Node.NODE_COUNT;
            return;
        }
        this.maxUnLockNodes = maxUnLockNodes;
    }

    public void setOnCreateLockListener(OnCreateLockListener onCreateLockListener) {
//        if (onUnLockListener != null) {
//            throw new RuntimeException("You can just set either the setOnUnLockListener() or the setOnCreateLockListener() because of their mutual exclusion ");
//        }
        this.onCreateLockListener = onCreateLockListener;
    }

    public void setOnUnLockListener(OnUnLockListener onUnLockListener) {
//        if (onCreateLockListener != null) {
//            throw new RuntimeException("You can just set either the setOnUnLockListener() or the setOnCreateLockListener() because of their mutual exclusion ");
//        }
        this.onUnLockListener = onUnLockListener;
    }

    private OnCreateLockListener onCreateLockListener;
    private OnUnLockListener onUnLockListener;

    public interface OnCreateLockListener {
        int ERROR_MIN_NODES = 1;
        int ERROR_MAX_NODES = 2;
        int ERROR_PASSWORD_UN_MATCHABLE = 3;

        void onCreateLockSuccess(List<Node> nodes, String createdLink);

        void onCreateLockRepeatSuccess(List<Node> nodes, String createdLink);

        /**
         * @param nodes 已连接的所有节点
         * @param error {@link #ERROR_MIN_NODES}{@link #ERROR_MAX_NODES}{@link #ERROR_PASSWORD_UN_MATCHABLE}
         */
        void onCreateLockFailed(List<Node> nodes, int error);
    }

    public interface OnUnLockListener {
        int ERROR_MIN_NODES = 1;
        int ERROR_MAX_NODES = 2;
        int ERROR_WRONG_PASSWORD = 3;

        void onUnLockSuccess(List<Node> linkedNodes);

        /**
         * @param linkedNodes 已连接的所有节点
         * @param error       {@link #ERROR_MIN_NODES}{@link #ERROR_MAX_NODES}{@link #ERROR_WRONG_PASSWORD}
         */
        void onUnLockFailed(List<Node> linkedNodes, int error);

    }

    /**
     * 默认节点的实现，可自行继承Node实现自定义节点
     */
    private static class DefaultNode extends Node {
        private static final int INNER_RADIUS = 10;
        private static final int OUTER_RADIUS = 50;
        private static final int OUTER_STROKE_WIDTH = 5;
        private static final double OUTER_RADIUS_POW = Math.pow(OUTER_RADIUS, 2);
        private Paint innerPaint, outerPaint;

        public DefaultNode(int index) {
            super(index);
            innerPaint = new Paint();
            innerPaint.setAntiAlias(true);
            innerPaint.setStyle(Paint.Style.FILL);
            outerPaint = new Paint();
            outerPaint.setAntiAlias(true);
            outerPaint.setStyle(Paint.Style.STROKE);
            outerPaint.setStrokeWidth(OUTER_STROKE_WIDTH);
        }

        @Override
        public boolean contains(float x, float y) {
            return Math.pow((pointX - x), 2) + Math.pow((pointY - y), 2) <= OUTER_RADIUS_POW;
        }

        @Override
        boolean contains(float x0, float y0, float x1, float y1) {
            return pointToLine(x0, y0, x1, y1, pointX, pointY) <= OUTER_RADIUS;
        }

        // 点到直线的最短距离的判断 点（x0,y0） 到由两点组成的线段（x1,y1） ,( x2,y2 )
        private double pointToLine(float x1, float y1, float x2, float y2, float x0,
                                   float y0) {
            double space = 0;
            double a, b, c;
            a = lineSpace(x1, y1, x2, y2);// 线段的长度
            b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
            c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
            if (c <= 0.000001 || b <= 0.000001) {
                space = 0;
                return space;
            }
            if (a <= 0.000001) {
                space = b;
                return space;
            }
            if (c * c >= a * a + b * b) {
                space = b;
                return space;
            }
            if (b * b >= a * a + c * c) {
                space = c;
                return space;
            }
            double p = (a + b + c) / 2;// 半周长
            double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
            space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
            return space;
        }

        // 计算两点之间的距离
        private double lineSpace(float x1, float y1, float x2, float y2) {
            double lineLength = 0;
            lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                    * (y1 - y2));
            return lineLength;
        }

        @Override
        public void onLayout(boolean changed, int childLeft, int childTop, int childRight, int childBottom, int position) {
            final int circleWidthHalf = OUTER_RADIUS + OUTER_STROKE_WIDTH;
            final int nodeWidthSpacing = (int) ((childRight - childLeft - NODE_RAW_COUNT * circleWidthHalf * 2) * 1.0 / (NODE_RAW_COUNT - 1));
            final int nodeHeightSpacing = (int) ((childBottom - childTop - NODE_COLUMN_COUNT * circleWidthHalf * 2) * 1.0 / (NODE_COLUMN_COUNT - 1));
            int x = position % NODE_RAW_COUNT;
            int y = position / NODE_COLUMN_COUNT;
            //从父布局左边开始绘制+横向间隔*x+(当前圆圈的半径+圆圈厚度)+(圆的直径*横向当前第几个圆圈《也就是前面圆圈所占的宽度》)
            pointX = childLeft + nodeWidthSpacing * x + circleWidthHalf + (circleWidthHalf * 2 * x);
            //从父布局顶部开始绘制+纵向间隔*x+(当前圆圈的半径+圆圈厚度)+(圆的直径*纵向当前第几个圆圈《也就是前面圆圈所占的高度》)
            pointY = childTop + nodeHeightSpacing * y + circleWidthHalf + (circleWidthHalf * 2 * y);
        }

        @Override
        public void onDraw(Canvas canvas, int nodeStatus) {
            canvas.drawCircle(pointX, pointY, INNER_RADIUS, innerPaint);
            canvas.drawCircle(pointX, pointY, OUTER_RADIUS, outerPaint);
        }

        @Override
        public void drawNormal(Canvas canvas) {
            innerPaint.setColor(0xffb3b3b3);
            outerPaint.setColor(0xffffffff);
        }

        @Override
        public void drawLinked(Canvas canvas) {
            innerPaint.setColor(0xff00ff00);
            outerPaint.setColor(0xff00ff00);
        }

        @Override
        public void drawLinkedError(Canvas canvas) {
            innerPaint.setColor(0xffff0000);
            outerPaint.setColor(0xffff0000);
        }
    }

    public static abstract class Node {
        public static final int NODE_RAW_COUNT = 3;
        public static final int NODE_COLUMN_COUNT = 3;
        public static final int NODE_COUNT = NODE_RAW_COUNT * NODE_COLUMN_COUNT;
        private boolean linked = false;
        protected float pointX;
        protected float pointY;
        protected int index;

        public Node(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        final void draw(Canvas canvas, int nodeStatus) {
            if (!linked) {
                drawNormal(canvas);
            } else if (NODE_LINKED_ERROR == nodeStatus) {
                drawLinkedError(canvas);
            } else {
                drawLinked(canvas);
            }
            onDraw(canvas, nodeStatus);
        }

        public boolean isLinked() {
            return linked;
        }

        public void setLinked(boolean linked) {
            this.linked = linked;
        }

        public float getPointX() {
            return pointX;
        }

        public void setPointX(float pointX) {
            this.pointX = pointX;
        }

        public float getPointY() {
            return pointY;
        }

        public void setPointY(float pointY) {
            this.pointY = pointY;
        }

        /**
         * 是否包含了当前坐标
         *
         * @param x
         * @param y
         * @return
         */
        abstract boolean contains(float x, float y);

        /**
         * 是否穿过了某条线段
         *
         * @param x0
         * @param y0
         * @param x1
         * @param y1
         * @return
         */
        abstract boolean contains(float x0, float y0, float x1, float y1);

        abstract void onLayout(boolean changed, int left, int top, int right, int bottom, int position);

        abstract void onDraw(Canvas canvas, int nodeStatus);

        /**
         * 未触摸过的节点
         *
         * @param canvas
         */
        abstract void drawNormal(Canvas canvas);

        /**
         * 连接成功的节点
         *
         * @param canvas
         */
        abstract void drawLinked(Canvas canvas);

        /**
         * 连接失败的节点
         *
         * @param canvas
         */
        abstract void drawLinkedError(Canvas canvas);

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable("superData", parcelable);
        bundle.putInt("nodeStatus", nodeStatus);
        bundle.putInt("state", this.state);
        bundle.putInt("unLockErrorCount", this.unLockErrorCount);
        bundle.putString("matchableLink", matchableLink);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            nodeStatus = bundle.getInt("nodeStatus");
            this.state = bundle.getInt("state");
            this.unLockErrorCount = bundle.getInt("unLockErrorCount");
            this.matchableLink = bundle.getString("matchableLink");
            state = bundle.getParcelable("superData");
        }
        super.onRestoreInstanceState(state);
    }
}
