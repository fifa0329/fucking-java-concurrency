package com.oldratlee.fucking.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class WrongCounterDemo {
    private static final int INC_COUNT = 100000000;

    volatile int counter = 0;


//    （1）volatile 无效
//    （2）使用synchronized也无效

    //可以使用这个原子记数来解决问题
    static AtomicInteger atomicInteger = new AtomicInteger();


    public static void main(String[] args) throws Exception {
        WrongCounterDemo demo = new WrongCounterDemo();

        System.out.println("Start task thread!");


        //果然，这么一改动之后，使用synchronized就有用了
        //        原因：synchronize主要的意义是：当一个线程占据这个方法时候，必须等待其运行完，其他线程才能进入
        //        原写法：
        //
        //        Thread thread1 = new Thread(demo.getConcurrencyCheckTask());
        //        thread1.start();
        //        Thread thread2 = new Thread(demo.getConcurrencyCheckTask());
        //        thread2.start();
        //        其实是两个实例方法！！！


        //下面的这种写法，光是volatile没有用，必须要synchronized run

        //理解volatile
//        http://www.ibm.com/developerworks/cn/java/j-jtp06197.html
        ConcurrencyCheckTask task = demo.getConcurrencyCheckTask();
        Thread thread1 = new Thread(task);
        thread1.start();
        Thread thread2 = new Thread(task);
        thread2.start();

        thread1.join();
        thread2.join();

        int actualCounter = demo.counter;
        int expectedCount = INC_COUNT * 2;
        int atomic = atomicInteger.intValue();

        if (actualCounter != expectedCount) {
            // 在我的开发机上，几乎必现！即使counter上加了volatile。（简单安全的解法：使用AtomicInteger）
            System.err.printf("Fuck! Got wrong count!! actual %s, expected: %s.", actualCounter, expectedCount);
        } else {
            System.out.println("Wow... Got right count!");
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < INC_COUNT; ++i) {
                ++counter;
                atomicInteger.getAndAdd(1);
            }
        }
    }
}
