package interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAction<R> extends Serializable {
    //Both T & R can be null or Void
    void action(R data);
}
