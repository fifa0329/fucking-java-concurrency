package com.oldratlee.fucking.concurrency;

import com.oldratlee.fucking.concurrency.util.Utils;

/**
 * //    无同步的修改在另一个线程中读不到
 //
 //    Demo类com.oldratlee.fucking.concurrency.NoPublishDemo。
 //
 //    Demo说明
 //
 //    主线程中设置属性stop为true，以控制在main启动的任务线程退出。
 //
 //    问题说明
 //
 //    在主线程属性stop为true后，但任务线程持续运行，即任务线程中一直没有读到新值。
 //    其他线程能访问这个字段，但是不能第一时间知道他的修改！！！
 //    stop加上volatile关键字之后，这个字段的修改就能被其他线程访问到
 * <a href="http://hllvm.group.iteye.com/group/topic/34932">请问R大 有没有什么工具可以查看正在运行的类的c/汇编代码</a>提到了<b>代码提升</b>的问题。
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see <a href="http://hllvm.group.iteye.com/group/topic/34932">请问R大 有没有什么工具可以查看正在运行的类的c/汇编代码</a>
 */
public class NoPublishDemo {

//



    volatile boolean stop = false;

    public static void main(String[] args) {
        // LoadMaker.makeLoad();

        NoPublishDemo demo = new NoPublishDemo();

        Thread thread = new Thread(demo.getConcurrencyCheckTask());
        thread.start();

        Utils.sleep(3000);
        System.out.println("Set stop to true in main!");
        demo.stop = true;
        Utils.sleep(3000);

        System.out.println("Exit main.");
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        public void run() {
            System.out.println("ConcurrencyCheckTask started!");
            // 如果主线中stop的值可见，则循环会退出。
            // 在我的开发机上，几乎必现循环不退出！（简单安全的解法：在running属性上加上volatile）
            while (!stop) {
            }
            System.out.println("ConcurrencyCheckTask stopped!");
        }
    }
}
