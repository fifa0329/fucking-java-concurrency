fucking-java-concurrency
==========================


**Writing correct program is hard and writing correct concurrent program is harder.** There are lots of things that can go wrong in concurrent program than a sequential program. Concurrency (Threading) is very important feature of Java. If you are interested to write concurrent program in Java, then there are lots of things what you have to take care about. Therefore, I would like to cover some common problems, which you could might be encounter with in concurrency. These are some points which are important to understand about concurrent programming. It is necessary to know about reasons and solutions for these problems. I have taken help from really a good book (Java Concurrency In Practice) for concurrent programming in Java to write this blog. Let us discuss these problems one by one.  
 
- Atomicity  
In concurrent environment two or many threads can run concurrently, if all threads are trying to update some shared resource concurrently, there might be chance that threads trying to read shared resource get stale data and produce wrong outputs. To resolve this issue, it is necessary to make such critical code atomic. Atomicity is to create any operation atomic, in such way that operation can be performed in a single attempt. If a critical section of code is getting execute by only one thread at a time and no other thread can enter in this section until that thread complete its execution. That means this critical section is atomic. You can make any critical section of code atomic by synchronization.  
2. Visibility  
Visibility of shared object could be different for different threads. This is one of the problems of multi-threading environment. Suppose there are two threads, one is writing any shared variable and other is reading from shared variable. When reading and writing will be occur in different threads, there is no guarantee that reader thread will see the value written by writer thread. This problem of multi-threading is known as visibility issue of shared resource. This problem can be resolved with proper synchronization of shared resource.  
3. Reordering  
Order of execution of code can be different as it is written in class. Because JVM rearrange this code according to its convenience, that is known as reordering. Today’s machines do run on multiple processors. To utilize these processors JVM split code to run with different processors, therefore order of code could be change during execution. There might be change that any critical section of code can generate wrong result after reordering of code. If you do not want to change the order of execution of code written then you will have to synchronize your critical code.  
4. Race Conditions  
A race condition occurs when the correctness of computation depends on the relative time or interleaving of multiple threads by the runtime. In other words, when getting the right answers relies on lucky timing. The most common type of race condition is check-then-act, where a potentially stale observation is used to make a decision on what to do next. You can resolve this problem by make your critical section atomic using proper synchronization.  
5. Livelock  
Livelock is a form of liveness failure in which a thread, while not blocked, still cannot make progress because it keeps retrying an operation that will always fail. Livelock often occurs in transaction a messaging applications, where the messaging infrastructure rolls back a transaction if a message cannot be processed successfully, and puts it back at the head of the queue.  
6. Starvation  
Starvation occur when a thread is denied and not getting access to resource to progress its task. The most common starved resource is CPU cycles. Starvation in Java application can be caused by inappropriate use of thread priorities. For example two thread of same priority is trying to access a common resource and each thread allowing other thread to have its access first. In such situation no thread could get access of resource. That means both threads are suffering from starvation.  
7. Deadlock  
Deadlock occur when two of many thread are waiting for two or more resources, where each thread need to get access on all resources to progress and those resources are acquired by different threads and waiting for other resources to be release, which will not be possible ever. For example, two threads A and B need both resources X and Y to perform their tasks. If thread A acquires resource X and thread B acquires Y and now both threads are waiting for resources to be release by other thread. Which is not possible and this is called deadlock. You need to take care of the way of your synchronization if this is going to be a cause of deadlock.  

#Conclusion  
These are few common problems which are most frequently occurs in concurrency in Java. You need to take care about proper synchronization of your code in multi-threaded environment otherwise it could be a cause of big issues.  
If you want to know more about these issues deeply, you could read my upcoming blogs on same problems with deep detail on issues and their appropriate solutions to make threading robust in Java.  
This entry was posted in Interview Questions, Java and tagged Multithreading, Synchronization, Threads. Bookmark the permalink.




---












:point_right: 通过Demo演示出`Java`中并发问题。
   
可以观察到的实际现象 :see_no_evil: 比 说说的并发原则 :speak_no_evil: 更直观更可信。

`Java`语言标准库支持线程，语言本身（如`GC`）以及应用（服务器端`The Server side`）中会重度使用多线程。

并发程度设计在分析和实现中，复杂度大大增加。如果不系统理解和充分分析并发逻辑，随意写代码，这样的程序用 **『碰巧』** 能运行出正确结果 来形容一点都不为过。  
\# 这里的Demo没有给出解释和讨论，要深入了解请参见[并发方面的系统的资料](ConcurrencyMaterial.md)。

你在开发中碰到的并发问题的例子，欢迎提供（[提交Issue](https://github.com/oldratlee/fucking-java-concurrency/issues))和分享（[Fork后提交代码](https://github.com/oldratlee/fucking-java-concurrency/fork)）！ :kissing_heart:

:beer: 无同步的修改在另一个线程中读不到
----------------------------------

Demo类[`com.oldratlee.fucking.concurrency.NoPublishDemo`](src/main/java/com/oldratlee/fucking/concurrency/NoPublishDemo.java)。

### Demo说明

主线程中设置属性`stop`为`true`，以控制在`main`启动的任务线程退出。

### 问题说明

在主线程属性`stop`为`true`后，但任务线程持续运行，即任务线程中一直没有读到新值。

### 快速运行

```bash
mvn compile exec:java -Dexec.mainClass=com.oldratlee.fucking.concurrency.NoPublishDemo
```

:beer: `long`变量读到无效值
----------------------------------

`long`变量读写不是原子的，会分为2次4字节操作。     
Demo类[`com.oldratlee.fucking.concurrency.InvalidLongDemo`](src/main/java/com/oldratlee/fucking/concurrency/InvalidLongDemo.java)。

### Demo说明

主线程修改`long`变量，每次写入的`long`值的高4字节和低4字节是一样的。在任务线程中读取`long`变量。

### 问题说明

任务线程中读到了高4字节和低4字节不一样的`long`变量，即是无效值（从来没有设置过的值）。

### 快速运行

```bash
mvn compile exec:java -Dexec.mainClass=com.oldratlee.fucking.concurrency.InvalidLongDemo
```

:beer: `HashMap`的死循环
----------------------------------

这个问题在[疫苗：Java HashMap的死循环](http://coolshell.cn/articles/9606.html)等多个地方都有讲解。    
Demo类[`com.oldratlee.fucking.concurrency.HashMapHangDemo`](src/main/java/com/oldratlee/fucking/concurrency/HashMapHangDemo.java)，可以复现这个问题。

### Demo说明

主线程中开启2个任务线程执行`HashMap`的`put`操作。主线程做`get`操作。

### 问题说明

通过没有持续的输出判定主线程`Block`，即`HashMap`的出现死循环。

### 快速运行

```bash
mvn compile exec:java -Dexec.mainClass=com.oldratlee.fucking.concurrency.HashMapHangDemo
```

:beer: 无同步的并发计数结果不对
----------------------------------

Demo类[`com.oldratlee.fucking.concurrency.WrongCounterDemo`](src/main/java/com/oldratlee/fucking/concurrency/WrongCounterDemo.java)。

### Demo说明

主线程中开启2个任务线程执行并发递增计数。主线程最终结果检查。

### 问题说明

计数值不对。

### 快速运行

```bash
mvn compile exec:java -Dexec.mainClass=com.oldratlee.fucking.concurrency.WrongCounterDemo
```

:beer: 在易变域上的同步
-------------------------

常看到在易变域上的同步代码，并且写的同学会很自然觉得这样是安全和正确的。      
\# 问题分析见文章链接：[在易变域上的同步](http://www.ibm.com/developerworks/cn/java/j-concurrencybugpatterns/#N100DA)，对应的英文文章：[Synchronization on mutable fields](http://www.ibm.com/developerworks/library/j-concurrencybugpatterns/#N100E7)    
Demo类[`com.oldratlee.fucking.concurrency.SynchronizationOnMutableFieldDemo`](src/main/java/com/oldratlee/fucking/concurrency/SynchronizationOnMutableFieldDemo.java)。

### Demo说明

主线程中开启2个任务线程执行`addListener`。主线程最终结果检查。

### 问题说明

最终`Listener`的个数不对。

### 快速运行

```bash
mvn compile exec:java -Dexec.mainClass=com.oldratlee.fucking.concurrency.SynchronizationOnMutableFieldDemo
```

:beer: 对称锁死锁
-------------------------

\# 问题分析见文章链接：[对称锁死锁](http://www.ibm.com/developerworks/cn/java/j-concurrencybugpatterns/#N101B4)，对应的英文文章：[Synchronization on mutable fields](http://www.ibm.com/developerworks/library/j-concurrencybugpatterns/#N101C1)    
Demo类[`com.oldratlee.fucking.concurrency.SymmetricLockDeadlockDemo`](src/main/java/com/oldratlee/fucking/concurrency/SymmetricLockDeadlockDemo.java)。

### Demo说明

主线程中开启2个任务线程执行。

### 问题说明

任务线程死锁。

### 快速运行

```bash
mvn compile exec:java -Dexec.mainClass=com.oldratlee.fucking.concurrency.SymmetricLockDeadlockDemo
```

一些并发的问题讨论和资料
-------------------------

- [ibm developerworks - 多核系统上的`Java`并发缺陷模式（`bug patterns`）](http://www.ibm.com/developerworks/cn/java/j-concurrencybugpatterns/)
- [stackoverflow - What is the most frequent concurrency issue you've encountered in Java?](http://stackoverflow.com/questions/461896/what-is-the-most-frequent-concurrency-issue-youve-encountered-in-java)
- [Java Concurrency Gotchas](http://www.slideshare.net/alexmiller/java-concurrency-gotchas-3666977)
- [Common problems of concurrency (Multi-Threading) in Java](http://www.somanyword.com/2014/03/common-problems-of-concurrency-multi-threading-in-java/)
- [java tutorial - Concurrency](http://docs.oracle.com/javase/tutorial/essential/concurrency/index.html)
