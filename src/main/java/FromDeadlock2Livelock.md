#Deadlock

Deadlock describes a situation where two or more threads are blocked forever, waiting for each other. Here's an example.

Alphonse and Gaston are friends, and great believers in courtesy.
A strict rule of courtesy is that when you bow to a friend, you must remain bowed until your friend has a chance to return the bow.
Unfortunately, this rule does not account for the possibility that two friends might bow to each other at the same time.
This example application, Deadlock, models this possibility:


public class Deadlock {
    static class Friend {
        private final String name;
        public Friend(String name) {
            this.name = name;
        }
        public String getName() {
            return this.name;
        }
        public synchronized void bow(Friend bower) {
            System.out.format("%s: %s"
                + "  has bowed to me!%n",
                this.name, bower.getName());
            bower.bowBack(this);
        }
        public synchronized void bowBack(Friend bower) {
            System.out.format("%s: %s"
                + " has bowed back to me!%n",
                this.name, bower.getName());
        }
    }  
    
    public static void main(String[] args) {
        final Friend alphonse =
            new Friend("Alphonse");
        final Friend gaston =
            new Friend("Gaston");
        new Thread(new Runnable() {
            public void run() { alphonse.bow(gaston); }
        }).start();
        new Thread(new Runnable() {
            public void run() { gaston.bow(alphonse); }
        }).start();
    }
}


When Deadlock runs, it's extremely likely that both threads will block when they attempt to invoke bowBack.
Neither block will ever end, because each thread is waiting for the other to exit bow.

---

#Livelock

A thread often acts in response to the action of another thread.
If the other thread's action is also a response to the action of another thread, then livelock may result.
As with deadlock, livelocked threads are unable to make further progress.
However, the threads are not blocked — they are simply too busy responding to each other to resume work.
This is comparable to two people attempting to pass each other in a corridor:
Alphonse moves to his left to let Gaston pass, while Gaston moves to his right to let Alphonse pass.
Seeing that they are still blocking each other, Alphone moves to his right, while Gaston moves to his left. They're still blocking each other, so...

---


http://stackoverflow.com/questions/1036364/good-example-of-livelock

Flippant comments aside, one example which is known to come up is in code which tries to detect and handle deadlock situations.
If two threads detect a deadlock, and try to "step aside" for each other,
without care they will end up being stuck in a loop always "stepping aside" and never managing to move forwards.
By "step aside" I mean that they would release the lock and attempt to let the other one acquire it.
We might imagine the situation with two threads doing this (pseudocode):



// thread 1
getLocks12(lock1, lock2)
{
  lock1.lock();
  while (lock2.locked())
  {
    // attempt to step aside for the other thread
    lock1.unlock();
    wait();
    lock1.lock();
  }
  lock2.lock();
}

// thread 2
getLocks21(lock1, lock2)
{
  lock2.lock();
  while (lock1.locked())
  {
    // attempt to step aside for the other thread
    lock2.unlock();
    wait();
    lock2.lock();
  }
  lock1.lock();
}




Race conditions aside, what we have here is a situation where both threads,
if they enter at the same time will end up running in the inner loop without proceeding.
Obviously this is a simplified example.
A naiive fix would be to put some kind of randomness in the amount of time the threads would wait.

The proper fix is to always respect the lock heirarchy. Pick an order in which you acquire the locks and stick to that.
For example if both threads always acquire lock1 before lock2, then there is no possibility of deadlock.