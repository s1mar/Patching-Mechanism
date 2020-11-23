package patch;

import java.rmi.Remote;
import java.rmi.RemoteException;

//The interface between the target and out patch overseer
public interface IPatchOverseer_Target extends Remote {

    int NOT_ALLOWED = 1; //if the admin has disabled any patching on the target side
    int WAIT = 2; // The target needs more time to clean-up and store state before patching begins
    int GO_AHEAD = 0; //The target is in a state ready to be patched

    void patchInitiated(int targetState) throws RemoteException;

}
