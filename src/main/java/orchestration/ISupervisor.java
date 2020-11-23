package orchestration;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISupervisor extends Remote {
    String serviceName = Supervisor.class.getSimpleName();
    public static int REGISTRY_PORT = 2596;
    boolean startTarget() throws RemoteException;
    void killTarget() throws RemoteException;

}
