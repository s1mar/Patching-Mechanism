package demo;

import patch.IPatchOverseer_Target;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ITargetApp extends Remote {
    String SERVICE_NAME = DemoTargetApp.class.getSimpleName();
    int REGISTRY_PORT = 2597;
    void patchingInitiated(IPatchOverseer_Target listener) throws RemoteException;
    void patching_Shutdown() throws RemoteException;
    int patchBuildNumber() throws RemoteException;
    void patchHandoverComplete() throws RemoteException;
    String patchRegion() throws RemoteException;
}
