package com.smt.Thread;


import org.apache.log4j.Logger;

import java.util.concurrent.*;

//线程池管理
public class ThreadManager {

    private static String TAG = "ThreadManager";

    public final static Logger logger = Logger.getLogger(TAG);


    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(
                50,
                50,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
                );

    //设置线程池任务
    public static void setThreadToPool(Runnable runnable) {
        try{
            logger.info("开始执行线程池任务");
            executor.submit(runnable);
        }catch (RejectedExecutionException e){
            logger.info("线程池已满,拒绝加入新任务:" + e);
        }
    }






}

