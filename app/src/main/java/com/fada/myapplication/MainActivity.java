package com.fada.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ArrayList<String> strings;
    private Toolbar mToolbar;
    private RadioGroup mFabButton;
    private LayoutInflater mInflater;
    private View headerView;
    private View footerView;
    private static AnimRFRecyclerView mRecyclerView;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    // 刷新完成后调用，必须在UI线程中
                    mRecyclerView.refreshComplate();


                    break;

                default:
                    // 加载更多完成后调用，必须在UI线程中
                    mRecyclerView.loadMoreComplate();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFabButton = (RadioGroup) findViewById(R.id.main_radio);
        initToolbar();
        strings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {

            strings.add("测试一下");
        }
        mRecyclerView = (AnimRFRecyclerView) findViewById(R.id.recyclerView);
        mInflater = LayoutInflater.from(this);
        headerView = mInflater.inflate(R.layout.header_view, null);
        footerView = mInflater.inflate(R.layout.footer_view, null);

        // 添加头部和脚部，如果不添加就使用默认的头部和脚部（头部可以有多个）
        mRecyclerView.addHeaderView(headerView);
        // 设置头部的最大拉伸倍率，默认1.5f，必须写在setHeaderImage()之前
        mRecyclerView.setScaleRatio(1.5f);
        // 设置下拉时拉伸的图片，不设置就使用默认的
        mRecyclerView.setHeaderImage((ImageView) headerView.findViewById(R.id.iv_hander));


        mRecyclerView.addFootView(footerView);


        // 设置刷新动画的颜色（可选）
        mRecyclerView.setColor(Color.RED, Color.BLUE);
        // 设置头部恢复动画的执行时间，默认1000毫秒（可选）
        mRecyclerView.setHeaderImageDurationMillis(1000);
        // 设置拉伸到最高时头部的透明度，默认0.5f（可选）
        mRecyclerView.setHeaderImageMinAlpha(0.6f);


        //TODO 使用重写的布局管理器

        // 使用重写后的线性布局管理器
        mRecyclerView.setLayoutManager(new AnimRFLinearLayoutManager(this));

        // 使用重写后的格子布局管理器
//        mRecyclerView.setLayoutManager(new AnimRFGridLayoutManager(this, 2));

        // 使用重写后的瀑布流布局管理器
//        mRecyclerView.setLayoutManager(new AnimRFStaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        //TODO 默认的布局管理器
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));//这里用线性宫格显示 类似于grid view
        //这里用线性宫格显示 类似于瀑布流-垂直滑动.两排
//        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
        //这里用线性宫格显示 类似于瀑布流 OrientationHelper.HORIZONTA表示横向滑动.2表示两列
//      recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.HORIZONTAL));

        //TODO 设置Adapter
        mRecyclerView.setAdapter(new MyRecyclerViewAdapter());

        //TODO 设置监听之标题动态隐藏
        mRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });

        //TODO  设置刷新和加载更多数据的监听，分别在onRefresh()和onLoadMore()方法中执行刷新和加载更多操作
        mRecyclerView.setLoadDataListener(new AnimRFRecyclerView.LoadDataListener() {
            @Override
            public void onRefresh() {
                // 开启线程刷新数据
                new Thread(() -> {
                    SystemClock.sleep(2000);
//                    for (int i = 0; i < 5; i++) {
//
//                        strings.add(0,"测试一下");
//                    }
                    handler.sendEmptyMessage(0);
                }
                ).start();
            }

            @Override
            public void onLoadMore() {
                // 开启线加载更多数据
                new Thread(() -> {
                    for (int i = 0; i < 5; i++) {

                        strings.add("测试一下");
                    }
                    SystemClock.sleep(2000);
                    handler.sendEmptyMessage(1);
                }
                ).start();
            }
        });


    }

    /**
     * 初始化Toolbar
     */
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }

    /**
     * 定义滑动监听抽象类.主要有于后面隐藏及显示标题栏目及底栏
     */
    public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }
            if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                scrolledDistance += dy;
            }
        }

        public abstract void onHide();

        public abstract void onShow();

    }

    /**
     * 隐藏头尾布局
     */
    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        mFabButton.animate().translationY(mFabButton.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * 显示头尾布局
     */
    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    /**
     * TODO 定义适配器Adapter
     */
   public class MyRecyclerViewAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.rc_item, null);

            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            //子类独有,所以需要强转
            // ((ItemViewHolder) holder).tv.setText(strings.get(position) + "当前角标是:=" + position);

        }

        @Override
        public int getItemCount() {
            return strings.size();
        }

        //定义Holder类,注意继承关系
        class ItemViewHolder extends RecyclerView.ViewHolder {

            private final ImageView tv;

            public ItemViewHolder(View itemView) {
                super(itemView);
                tv = (ImageView) itemView.findViewById(R.id.tv_item);

            }


        }
    }
}

