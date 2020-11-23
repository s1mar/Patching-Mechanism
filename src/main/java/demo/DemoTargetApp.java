package demo;

import com.google.gson.Gson;
import demo.entities.Job;
import org.beryx.textio.TextIoFactory;
import patch.IPatchOverseer_Target;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DemoTargetApp extends UnicastRemoteObject implements ITargetApp {

    private static final int BuildNumber= 200;
    private static final String Region = "us-east-1";

    private Registry registry;
    private static volatile DemoTargetApp mInstance;
    private static DemoTargetApp getInstance() throws Exception{
        if(mInstance == null){
            mInstance = new DemoTargetApp();
        }
        return mInstance;
    }

    protected DemoTargetApp() throws RemoteException {
        super();
    }

    public static void main (String[] args){
        try {
            getInstance().initialization();

        }catch (Exception ex){
            println(ex.toString());
        }
    }


    private void runJob(){
        //Run demo job code
        Path dirPath = Paths.get("").toAbsolutePath();
        Path workingDir = Paths.get(dirPath.toString(),"/work/").toAbsolutePath();
        Job job = Job.readJsonFromPath(Paths.get(workingDir.toString(),"job.json").toAbsolutePath().toString());
        if(job == null){
            println("\nJob not found");
        }else{
            println(new Gson().toJson(job));
        }

    }

    private void initialization() throws Exception{
        println("/---------------------Target App v1.0---------------------------/");
        registry = LocateRegistry.createRegistry(ITargetApp.REGISTRY_PORT);
        ITargetApp stub = getInstance();
        registry.bind(ITargetApp.SERVICE_NAME,stub);
        println("Server started");
        runJob();
    }

    @Override
    public void patchingInitiated(IPatchOverseer_Target listener) throws RemoteException {
        listener.patchInitiated(IPatchOverseer_Target.GO_AHEAD);
    }

    @Override
    public void patching_Shutdown() throws RemoteException {
        try {
            UnicastRemoteObject.unexportObject(registry,true);
            System.exit(0);
        }catch (Exception ex){
            println(ex.toString());
        }

    }

    @Override
    public int patchBuildNumber() throws RemoteException {
            return BuildNumber;
    }

    @Override
    public void patchHandoverComplete() throws RemoteException {
            println("\n Starting a new job");
            runJob();
    }

    @Override
    public String patchRegion() throws RemoteException {
        return Region;
    }

    private static void println(String s){

        System.out.println(s);
        TextIoFactory.getTextTerminal().println(s);

    }

}
