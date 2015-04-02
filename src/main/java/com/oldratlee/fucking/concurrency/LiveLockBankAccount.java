package com.oldratlee.fucking.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lingxiang.wang on 2015/4/2.
 *
 *
 *A LiveLock looks like a deadlock in the sense that two (or more) processes are blocking each others. But with the livelock,
 *  each process is waiting “actively”, trying to resolve the problem on its own (like reverting back its work and retry).
 *  A live lock occurs when the combination of these processes’s efforts to resolve the problem makes it impossible for them to ever terminate.
 Let’s take the example of the bank account again. Suppose another erroneous implementation of two simultaneous transfer operation.
 Here again, two threads tries to transfer money from one account to another one at the same time.
 But this time, instead of waiting for a lock to be released when a required account is locked,
 a thread will juste revert its work if any, and retry the whole operation in loop until successful :
 *
 *
 *
 *
 *
 * The result of this execution is somewhat similar to the one of the deadlock example, but this time the CPU is working harder.
 Typically the program will execute like this :

 transaction-1 withdraw 10$ from account “1”
 transaction-2 withdraw 10$ from account “2”
 transaction-1 failed to deposit in account “2” because “transaction-2″ already old the lock on that account. Account “1” refunded
 transaction-2 failed to deposit in account “1” because “transaction-1″ already old the lock on that account. Account “2” refunded
 transaction-1 withdraw 10.000000 from account “1”
 transaction-2 withdraw 10.000000 from account “2”
 transaction-1 failed to deposit in account “2” because “transaction-2″ already old the lock on that account. Account “1” refunded
 transaction-2 failed to deposit in account “1” because “transaction-1″ already old the lock on that account. Account “2” refunded
 and so on …


 这种livelock基本出现在
 tryLock！！！！！
 try获得lock会立刻返回当时能否取到lock！！
 */
public class LiveLockBankAccount {
    double balance;
    int id;
    Lock lock = new ReentrantLock();

    LiveLockBankAccount(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    boolean withdraw(double amount) {
        System.out.printf("%s 准备取 %s元钱\n", id,amount);

        if (this.lock.tryLock()) {
            // Wait to simulate io like database access ...
            try {Thread.sleep(3000);} catch (InterruptedException e) {}
            balance -= amount;
            System.out.printf("%s 取 %s元钱成功\n", id,amount);


            return true;
        }
        System.out.printf("%s 取 %s元钱失败\n", id,amount);
        return false;
    }

    boolean deposit(double amount) {
        System.out.printf("%s 准备存 %s元钱\n", id,amount);

        if (this.lock.tryLock()) {
            // Wait to simulate io like database access ...
            try {Thread.sleep(3000);} catch (InterruptedException e) {}
            balance += amount;
            System.out.printf("%s 存 %s元钱成功\n", id,amount);
            return true;
        }
        System.out.printf("%s 存 %s元钱失败\n", id,amount);

        return false;
    }

    public boolean tryTransfer(LiveLockBankAccount destinationAccount, double amount) {
        System.out.printf("%s 准备向 %s 转账 %s元钱\n", this.id,destinationAccount.id,amount);

        if (this.withdraw(amount)) {
            if (destinationAccount.deposit(amount)) {
                return true;
            } else {
                // destination account busy, refund source account.

                System.out.printf("对方没能存成功，%s 被退回了 %s元钱\n", id,amount);

                this.deposit(amount);
            }
        }

        return false;
    }

    public static void main(String[] args) {

        System.out.print("开始转账啦\n");

        final LiveLockBankAccount fooAccount = new LiveLockBankAccount(1, 500d);
        final LiveLockBankAccount barAccount = new LiveLockBankAccount(2, 500d);


        //照理来说应该简单的两次转账就结束了
        //但是问题没有那么简单
        new Thread(new LiveLockTransaction(fooAccount, barAccount, 10d), "transaction-1").start();
        new Thread(new LiveLockTransaction(barAccount, fooAccount, 10d), "transaction-2").start();

    }

}
class LiveLockTransaction implements Runnable {
    private LiveLockBankAccount sourceAccount, destinationAccount;
    private double amount;

    LiveLockTransaction(LiveLockBankAccount sourceAccount, LiveLockBankAccount destinationAccount, double amount) {
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
    }

    public void run() {
        while (!sourceAccount.tryTransfer(destinationAccount, amount))
            continue;
        System.out.printf("%s completed ", Thread.currentThread().getName());
    }

}