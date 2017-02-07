package com.sample;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Zero
 *         Created on 2017/2/7.
 */
public class Async {
    //Async.executor = undertow.getWorker();
    public static Executor executor = Executors.newCachedThreadPool();

    public static CompletableFuture run(Runnable runnable) {
        //CompletableFuture 默认线程池是 ForkJoinPool.commonPool, 这个线程池中的线程不能被阻塞,否则steam中的并发操作也会受到影响
        //所以这里使用自定义的线程池(这里要使用Undertow中的WorkerThreadPool)
        return CompletableFuture.runAsync(runnable, executor);
    }

}
