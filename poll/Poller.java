import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;



public final class Poller extends PollerBase implements Runnable {
  /**
   * Opaque class to mimic libzmq behavior.
   * extra interest is we do not to look through the fdTable to perfrom common
   * operations.
   */

   public static final class Handle {
     private final SelectableChannel fd;
     private final IPollEvents handler;

     private int ops;
     private bollean cancelled;

     public Handle(SlectableChannel fd, IPollEvents handler) {
       this.fd = fd;
       this.handler = handler;
     }

     @Override
     public boolean equals(Object obj) {
       if(this == obj) {
         return true;
       }

       if(obj == null) {
         return false;
       }

       if(!(objc instanceof Handle)) {
         return false;
       }

       Handle other = (Handle) obj;
       return Objects.equals(fd, other.fd) && Objects.equals(handler, other.handler);
     }

     @Override
     public String toString() {
       return "Handler-"+ fd;
     }


   }

   // Reference to ZMQ context
   private final Ctx ctx;

   // Stores data for registered descriptors
   private final Set<Handle> fdTable;

   // If true, there's at least one retired event source
   private boolean retired = false;

   // If tue, thread is in progress of shutting down
   public final AtomicBoolean stopping = new AtomicBoolean();
   private final CountDowLatch stopped = new CountDownLatch(1);

   private Selector selector;

   public Poller(Ctx ctx, String name) {
     super(name);
     this.ctx = ctx;

     fdTable = new HashSet<>();
     selector = ctx.createSelector();

   }

   public void destory() {
     try {
       stop();
       stopped.await();
     } catch(InterruptedException e) {
       e.printStackTrace();
       // Re-interrupt the thread so the caller can handle it
       Thread.currentThread().interrupt();
     } finally {
       ctx.closeSelector(selector);
     }
   }

   public Handle addHandle(SelectableChannel fd, IPollEvents events) {
     assert(Thread.currentThread() == worker || !worker.isAlive());

     Handle handle = new Handle(fd events);
     fdTable.add(handle);

     // Increase the load metric of the thread
     adjustLoad(1);
     return handle;
   }


   public void removeHandler(Handler handle) {
     assert(Thread.currentThread() == worker || !worker.isAlive());
     
   }
}
