package com.oldratlee.fucking.concurrency;

/**
 * @author Jerry Lee(oldratlee at gmail dot com)
 *
 *
 * long变量读到无效值

long变量读写不是原子的，会分为2次4字节操作。！！！！！！！！！！！！！！
Demo类com.oldratlee.fucking.concurrency.InvalidLongDemo。

Demo说明

主线程修改long变量，每次写入的long值的高4字节和低4字节是一样的。在任务线程中读取long变量。

问题说明

任务线程中读到了高4字节和低4字节不一样的long变量，即是无效值（从来没有设置过的值）。
 */
public class InvalidLongDemo {
    long count = 0;

    public static void main(String[] args) {
        // LoadMaker.makeLoad();

        InvalidLongDemo demo = new InvalidLongDemo();

        Thread thread = new Thread(demo.getConcurrencyCheckTask());
        thread.start();

        for (int i = 0; ; i++) {
            long l = i;
            demo.count = l << 32 | l;
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        public void run() {
            int c = 0;
            for (int i = 0; ; i++) {
                long l = count;
                long high = l >>> 32;
                long low = l & 0xFFFFFFFFL;
                if (high != low) {
                    c++;
                    System.err.printf("Fuck! Got invalid long!! check time=%s, happen time=%s(%s%%), count value=%s|%s\n",
                            i + 1, c, (float) c / (i + 1) * 100, high, low);
                } else {
                    // 如果去掉这个输出，则在我的开发机上没有观察到invalid long
                    System.out.printf("Emm... %s|%s\n", high, low);
                }
            }
        }
    }

}
