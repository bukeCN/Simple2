package com.live.simple2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaTest {
    private static CyclicBarrier cyclicBarrier;
    private static CountDownLatch countDownLatch;

    public static void main(String[] args){
        cyclicBarrier = new CyclicBarrier(2, new Runnable() {
            @Override
            public void run() {
                Log.e("sun","终极任务开始执行");
            }
        });

//        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 2;i++){
            new Thread(new LogTask(cyclicBarrier,i)).start();
        }

        Bitmap bitmap = new Bitmap();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inBitmap = bitmap;

        BitmapFactory.decodeFile("",options);




    }


    static class LogTask implements Runnable{
        private WeakReference<CyclicBarrier> cyclicBarrierWeakReference;
        private int taskNumber;

        public LogTask(CyclicBarrier cyclicBarrier, int taskNumber){
            cyclicBarrierWeakReference = new WeakReference<>(cyclicBarrier);
            this.taskNumber = taskNumber;
        }

        @Override
        public void run() {
            // 1. 执行任务
            int radomSlepp = new Random().nextInt(4000);
            // 模拟任务
//            try {
                Log.e("sun","任务：" + taskNumber + "号**开始执行");
//                Thread.sleep(radomSlepp);
                Log.e("sun","任务：" + taskNumber + "号**开始结束，开始等待。。。");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            // 2. 判断等待
            try {
                cyclicBarrierWeakReference.get().await();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
