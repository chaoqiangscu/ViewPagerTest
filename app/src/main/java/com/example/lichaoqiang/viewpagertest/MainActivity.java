package com.example.lichaoqiang.viewpagertest;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //布局文件中将要对应的控件
    private ViewPager viewPager;
    private TextView pagerTitle;
    private LinearLayout pagerIndicator;

    //将会在在ViewPager中会使用到的图片和标题资源
    //图片集合
    private final int[] imageIds = {
            R.drawable.a,
            R.drawable.b,
            R.drawable.c,
            R.drawable.d,
            R.drawable.e
    };
    // 标题集合
    private final int[] titleIds = {
            R.string.pager_title_one,
            R.string.pager_title_two,
            R.string.pager_title_three,
            R.string.pager_title_four,
            R.string.pager_title_five,
    };

    //在 PagerAdapter 适配器中使用到的图片集合
    private ArrayList<ImageView> imageViews;

    //记录上次高亮显示的指示圆点的位置，便于指示页面的滚动
    private int prePosition = 0;

    //判断页面是否在滚动
    private boolean isDragging = false;

    //这里由于非静态内部类会持有外部类的引用，会造成内存泄漏，但为了演示方便，就不做处理了，实际使用时要注意修改
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            int item = viewPager.getCurrentItem()+1;    //获取下一个待显示页面的位置
            viewPager.setCurrentItem(item); //显示下一个页面

            //自己驱动自己，延时、循环发送消息。消息的what字段无所谓，任意传一个就行，因为我们用不到分类处理机制
            handler.sendEmptyMessageDelayed(0,4000);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        pagerTitle = (TextView)findViewById(R.id.pager_title);
        pagerIndicator = (LinearLayout)findViewById(R.id.pager_indicator);
        init();  //初始化数据
        viewPager.setAdapter(new MyAdapter());  //设置适配器
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());    //添加监听器
        //设置中间位置。保证是imageViews的整数倍，这样在起始时，就会显示美景一的页面
        int item = Integer.MAX_VALUE/2 - Integer.MAX_VALUE/2%imageViews.size();
        viewPager.setCurrentItem(item);
        pagerTitle.setText(titleIds[prePosition]);
        handler.sendEmptyMessageDelayed(0,4000);    //发送消息
    }

    //ViewPager 的使用和 ListView 的使用很相似，都需配置一个适配器，ViewPager 使用的是 PagerAdapter，
    //因此需要为其准备一些数据
    private void init() {
        //设置图片
        imageViews = new ArrayList<ImageView>();
        int len = imageIds.length;
        for (int i = 0; i < len; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(imageIds[i]);   //设置具体图片
            imageViews.add(imageView);  //添加到集合中

            //添加指示页面的圆点
            ImageView point = new ImageView(this);  //指示圆点我们使用图片展示
            point.setBackgroundResource(R.drawable.point_selector); //使用自定义的 StateListDrawable
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(18, 18);  //设置圆点的大小
            if (i == 0) {
                point.setEnabled(true); //将第一个圆点设置为可用，则会显示红色
            } else {
                point.setEnabled(false);
                params.leftMargin = 18; //其它圆点设置为不可用，则会显示为灰色
            }
            point.setLayoutParams(params);  //设置布局参数
            pagerIndicator.addView(point);  //添加指示的圆点
        }
    }

    private class MyAdapter extends PagerAdapter {
        //有几个方法需要覆写

        //获取图片的总数，即ViewPager页面的总数
        @Override
        public int getCount(){
            //return imageViews.size();
            return Integer.MAX_VALUE;   //当然，此处也可以设置为其它比较大的数值
        }

        /**
         * 这个方法相当于 ListView 的 getView() 方法
         * @param container 就是ViewPager自身
         * @param position 当前实例化页面的位置
         * @return
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public Object instantiateItem(ViewGroup container, final int position){
            int realPosition = position%imageViews.size();  //设置位置
            final ImageView imageView = imageViews.get(realPosition);   //  获取对于位置的图片
            container.addView(imageView);   //添加到ViewPager中
            //设置图片的触摸事件，若使用监听器的 onPageScrollStateChanged() 方法，则可以删除以下触摸处理方法了
            /*imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            handler.removeCallbacksAndMessages(null);   //取消消息队列
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            //handler.removeCallbacksAndMessages(null);   //开启消息队列前，最好先清除之前的消息
                            //handler.sendEmptyMessageDelayed(0,4000);    //发送消息，开启消息队列
                            break;
                        case MotionEvent.ACTION_UP:
                            handler.removeCallbacksAndMessages(null);   //开启消息队列前，最好先清除之前的消息
                            handler.sendEmptyMessageDelayed(0,4000);    //发送消息，开启消息队列
                            break;
                    }
                    return false;    //返回true，表示要消费这次触摸事件,其它事件将不能再处理这次触摸操作。若要使用下面的点击事件，则此处需要设置为 false
                }
            });*/

            imageView.setTag(position);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag()%imageViews.size();
                    Toast.makeText(MainActivity.this, "你点击了图片", Toast.LENGTH_SHORT).show();

                }
            });

            return imageView;
        }

        /**
         * 比较 view 和 object 是否是同一个实例
         * @param view 页面
         * @param object 上述方法instantiateItem()返回的结果
         * @return
         */
        @Override
        public boolean isViewFromObject(View view,Object object){
            return view == object;
        }

        /**
         * 释放资源
         * @param container 就是ViewPager自身
         * @param position 准备要释放页面的位置
         * @param object 要释放的页面
         */
        @Override
        public void destroyItem(ViewGroup container,int position,Object object){
            //super.destroyItem(container, position, object);
            container.removeView((View)object); //需要进行一下强转
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        /**
         * function: 当前页面滚动的时候回调这个方法
         * @param position 当前页面的位置
         * @param positionOffset 滑动页面的百分比
         * @param positionOffsetPixels 滑动的像素数
         * @return
         */
        @Override
        public void onPageScrolled(int position,float positionOffset, int positionOffsetPixels){
            //此方法暂时用不到
        }

        /**
         * function: 当页面被选中时，会回调这个方法
         * @param position 被选中的页面的位置
         * @return
         */
        @Override
        public void onPageSelected(int position){
            int realPosition = position%imageViews.size();
            //设置对应页面的标题
            pagerTitle.setText(titleIds[realPosition]);
            //把之前高亮显示的指示圆点设为灰色
            pagerIndicator.getChildAt(prePosition).setEnabled(false);
            //将当前页面对应的指示圆点设置为高亮
            pagerIndicator.getChildAt(realPosition).setEnabled(true);
            //更新上次位置的变量值
            prePosition = realPosition;
        }

        /**
         * function: 当页面滚动状态变化时，会回调这个方法
         * 有三种状态：静止、滑动、拖拽（这里区别滑动和拖拽，以手指是否接触页面为准）
         * @param state 当前状态
         * @return
         */
        @Override
        public void onPageScrollStateChanged(int state){
            if (state == ViewPager.SCROLL_STATE_DRAGGING){  //拖拽状态
                isDragging = true;
                handler.removeCallbacksAndMessages(null);
            }else if (state == ViewPager.SCROLL_STATE_SETTLING){    //滑动状态，要区别拖拽
                //此处暂不做处理
            }else if (state == ViewPager.SCROLL_STATE_IDLE && isDragging){  //静止状态，且刚经历过拖拽状态
                isDragging = false;
                handler.removeCallbacksAndMessages(null);   //开启消息队列前，最好先清除之前的消息
                handler.sendEmptyMessageDelayed(0,4000);    //发送消息，开启消息队列
            }
        }
    }
}
