import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

// import zmq.SocketBase;
// import zm.ZQM;

public class PollItem {
  private final SocketBase socket;
  private final SelectableChannel channel;
  private final int zinterest;
  private final int interest;
  private int ready;

  public PollItem(SocketBase socket, int ops) {
    this(socket, null, ops);
  }

  public PollItem(SelectableChannel channel, int ops) {
    this(null, channel, ops);
  }

  private PollItem(SocketBase socket, SelectableChannel channel, int ops) {
    this.socket = socket;
    this.channel = channel;
    this.zinterest = ops;
    this.interest = init(ops);
  }

  public int init(int ops) {
    int interest = 0;
    if((ops & ZMQ.ZMQ_POLLIN) > 0) {
      interest |= SelectionKey.OP_READ;
    }

    if((ops & ZMQ.ZMQ_POLLOUT) > 0) {
      if(socket != null) {
        // ZMQ Socket get readiness frmo the mailbox
        interest |= SelectionKey.OP_READ;
      } else {
        interest |= SelectionKey.OP_WRITE;
      }
    }

    this.ready = 0;
    return interest;
  }

  
}
