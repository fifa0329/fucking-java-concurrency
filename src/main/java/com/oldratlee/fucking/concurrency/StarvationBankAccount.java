package com.oldratlee.fucking.concurrency;

/**
 * Created by lingxiang.wang on 2015/4/2.
 *
 *Lock starvation is all about thread priority.
 * It occurs when a thread, having lesser priority than other ones, is constantly waiting for a lock,
 * never able to take it because other thread(s) with higher priority are constanly aquiring the lock. Suppose our bank account example.
 * The bank adds a feature that constantly watch one’s account balance and send an email if that balance goes below zero (a monitor thread).
 * But in this implementation, the monitor thread is of a higher priority than the transaction threads.
 * Because of this, the transaction threads can take a very long time(say ever) to execute. See the example :
 *
 *
 *
 *
 *
 *
 * 饥饿的意思就是
 * 一个优先级较高的线程永远抢占着机会
 * 导致优先级较低的线程无法获得机会
 * In that example, a balance monitor is started on account foo. This balance monitor should stop when the balance it monitors falls under zero. An then, two transaction threads are started, each one taking 250$ from the foo account, wich started at 500$. That should normally leaves the account with 0$, wich should trigger the monitor to send email and terminates.
 If you try to execute this program, chances are that it will end successfully, but with abnormal execution time. This is because the transfer transactions have a hard time aquiring the lock on the foo account : the monitor almost always have it.
 The execution could be seen as this :

 BalanceMonitor lock account foo
 Transaction 1 tries to acquire lock on account foo. Cannot, because balance monitor hold it. Waiting …
 BalanceMonitor read balance
 BalanceMonitor unlock acccount
 BalanceMonitor lock account foo
 Transaction 1 retry to aquire lock, Cannot, BalanceMonitor still holding it.
 BalanceMonitor read balance
 BalanceMonitor unlock acccount
 BalanceMonitor lock account foo
 Transaction 1 retry to aquire lock, Cannot, BalanceMonitor still holding it.
 BalanceMonitor read balance
 BalanceMonitor unlock acccount
 And so on a lot of time until Transaction 1 and Transaction 2 finally execute
 */
public class StarvationBankAccount {
    private double balance;
    int id;

    StarvationBankAccount(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    synchronized double getBalance() {
        // Wait to simulate io like database access ...
        try {Thread.sleep(100l);} catch (InterruptedException e) {}

        System.out.format("一秒过去，monitor线程完成了一次查看\n");

        return balance;
    }

    synchronized void withdraw(double amount) {
        balance -= amount;
    }

    synchronized void deposit(double amount) {
        balance += amount;
    }

    synchronized void transfer(StarvationBankAccount to, double amount) {
        withdraw(amount);
        to.deposit(amount);
    }

    public static void main(String[] args) {
        final StarvationBankAccount fooAccount = new StarvationBankAccount(1, 500d);
        final StarvationBankAccount barAccount = new StarvationBankAccount(2, 500d);

        Thread balanceMonitorThread1 = new Thread(new BalanceMonitor(fooAccount), "BalanceMonitor");
        Thread transactionThread1 = new Thread(new StarvationTransaction(fooAccount, barAccount, 250d), "StarvationTransaction-1");
        Thread transactionThread2 = new Thread(new StarvationTransaction(fooAccount, barAccount, 250d), "StarvationTransaction-2");

        balanceMonitorThread1.setPriority(Thread.MAX_PRIORITY);
        transactionThread1.setPriority(Thread.MIN_PRIORITY);
        transactionThread2.setPriority(Thread.MIN_PRIORITY);

        // Start the monitor
        balanceMonitorThread1.start();

        // And later, transaction threads tries to execute.
        try {Thread.sleep(100l);} catch (InterruptedException e) {}
        transactionThread1.start();
        transactionThread2.start();

    }

}
class BalanceMonitor implements Runnable {
    private StarvationBankAccount account;
    BalanceMonitor(StarvationBankAccount account) { this.account = account;}
    boolean alreadyNotified = false;

    @Override
    public void run() {
        System.out.format("%s started execution%n", Thread.currentThread().getName());
        while (true) {
            if(account.getBalance() <= 0) {
                // send email, or sms, clouds of smoke ...
                break;
            }
        }
        System.out.format("%s : account has gone too low, email sent, exiting.", Thread.currentThread().getName());
    }

}
class StarvationTransaction implements Runnable {
    private StarvationBankAccount sourceAccount, destinationAccount;
    private double amount;

    StarvationTransaction(StarvationBankAccount sourceAccount, StarvationBankAccount destinationAccount, double amount) {
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
    }

    public void run() {
        System.out.format("%s started execution%n", Thread.currentThread().getName());
        sourceAccount.transfer(destinationAccount, amount);
        System.out.printf("%s completed execution%n", Thread.currentThread().getName());
    }

}
