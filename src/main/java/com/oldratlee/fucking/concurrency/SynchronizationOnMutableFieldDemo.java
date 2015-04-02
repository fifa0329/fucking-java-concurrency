package com.oldratlee.fucking.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 *
 *
 * :beer: 在易变域上的同步

常看到在易变域上的同步代码，并且写的同学会很自然觉得这样是安全和正确的。
# 问题分析见文章链接：在易变域上的同步，对应的英文文章：Synchronization on mutable fields
Demo类com.oldratlee.fucking.concurrency.SynchronizationOnMutableFieldDemo。

Demo说明

主线程中开启2个任务线程执行addListener。主线程最终结果检查。

问题说明

最终Listener的个数不对。
 */
public class SynchronizationOnMutableFieldDemo {
    static final int ADD_COUNT = 10000;

    static class Listener {
        // stub class
    }

    private volatile Object lock=new Object();
    private volatile List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public static void main(String[] args) throws Exception {
        SynchronizationOnMutableFieldDemo demo = new SynchronizationOnMutableFieldDemo();

        Thread thread1 = new Thread(demo.getConcurrencyCheckTask());
        thread1.start();
        Thread thread2 = new Thread(demo.getConcurrencyCheckTask());
        thread2.start();

        thread1.join();
        thread2.join();

        int actualSize = demo.listeners.size();
        int expectedSize = ADD_COUNT * 2;
        if (actualSize != expectedSize) {
            // 在我的开发机上，几乎必现！（简单安全的解法：final List字段并用并发安全的List，如CopyOnWriteArrayList）
            System.err.printf("Fuck! Lost update on mutable field! actual %s expected %s.\n", actualSize, expectedSize);
        } else {
            System.out.println("Emm... Got right answer!!");
        }
    }

    public void addListener(Listener listener) {

        //本来这里是listeners，但是我们要保证lock住的这个field是不会变化的，因此我们更乐于锁一个自己建的object，防止出现问题
        //private volatile Object lock=new Object();
//        http://www.ibm.com/developerworks/library/j-concurrencybugpatterns/#N100E7
        synchronized (lock) {
            List<Listener> results = new ArrayList<Listener>(listeners);
            results.add(listener);
            listeners = results;
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        public void run() {
            System.out.println("ConcurrencyCheckTask started!");
            for (int i = 0; i < ADD_COUNT; ++i) {
                addListener(new Listener());
            }
            System.out.println("ConcurrencyCheckTask stopped!");
        }
    }
}
