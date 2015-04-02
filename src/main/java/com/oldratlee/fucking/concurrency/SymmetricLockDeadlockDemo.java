package com.oldratlee.fucking.concurrency;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 *
 *
 *  对称锁死锁

# 问题分析见文章链接：对称锁死锁，对应的英文文章：Synchronization on mutable fields
Demo类com.oldratlee.fucking.concurrency.SymmetricLockDeadlockDemo。

Demo说明

主线程中开启2个任务线程执行。

问题说明

任务线程死锁。
 */
public class SymmetricLockDeadlockDemo {
    static final Object lock1 = new Object();
    static final Object lock2 = new Object();

    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(new ConcurrencyCheckTask1());
        thread1.start();
        Thread thread2 = new Thread(new ConcurrencyCheckTask2());
        thread2.start();


        System.out.print("The End\n");
    }

    private static class ConcurrencyCheckTask1 implements Runnable {
        @Override
        public void run() {
            System.out.println("ConcurrencyCheckTask1 started!");
            while (true) {
                synchronized (lock1) {
                    synchronized (lock2) {
                        //有可能这个占据了两个lock，导致一直在hello
                        //也有可能大家各自占据了一个lock，导致死锁
                        System.out.println("Hello1");
                    }
                }
            }
        }
    }

    private static class ConcurrencyCheckTask2 implements Runnable {
        @Override
        public void run() {
            System.out.println("ConcurrencyCheckTask2 started!");
            while (true) {
                synchronized (lock2) {
                    synchronized (lock1) {
                        System.out.println("Hello2");
                    }
                }
            }
        }
    }
}
