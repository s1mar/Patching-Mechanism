package orchestration;

import demo.DemoTargetApp;
import helper.JavaProcess;
import patch.PatchOverseer;

import java.io.File;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import static java.lang.System.out;

public class Supervisor implements ISupervisor {


    private static volatile Supervisor mInstance;

    private final HashMap<String,Process> mProcessMap = new HashMap<>();

    public static synchronized Supervisor getInstance(){

        if(mInstance == null){
            mInstance = new Supervisor();
        }

        return mInstance;
    }



    public static void main(String args[]){

        try {
            //setup the stage
            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            Supervisor supervisor = getInstance();
            ISupervisor stub = (ISupervisor) UnicastRemoteObject.exportObject(supervisor,REGISTRY_PORT);
            registry.bind(ISupervisor.serviceName,stub);
            out.println("Interface to Supervisor Online");

            supervisor.initialization();


        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void initialization() throws Exception{
        //start the target app
        boolean targetAppStarted = startTarget();
        if(targetAppStarted){
            out.println("Successfully Started the Target App");
        }
        else{
            out.println("Unable to start the Target App");
        }

        Thread.sleep(1000);

        //start the patching system
        startThePatchOverseer();
    }

    private void startThePatchOverseer() throws Exception{
      Process procPatchOverseer =  JavaProcess.exec(PatchOverseer.class,null);
      mProcessMap.put(PatchOverseer.class.getSimpleName(),procPatchOverseer);
    }

    @Override
    public boolean startTarget() throws RemoteException {

        try {
          /*  //Recompile the class before starting it
            String outputPath = Paths.get(Paths.get("").toAbsolutePath().toString(),"/build/classes/java/main/demo/").toAbsolutePath().toString();
            out.println("Output path for class: "+outputPath);
            String command = String.format("javac %s -d %s",DemoTargetApp.class.getSimpleName()+".java",outputPath );
            String srcPath = Paths.get(Paths.get("").toAbsolutePath().toString(),"/src/main/java/demo/").toAbsolutePath().toString();
            out.println("Javac command: "+command);
            out.println("src path: "+srcPath);
            File absPathName = new File(srcPath);
            out.println("absPathName: "+absPathName.getAbsolutePath());
            Process procCompileClass=Runtime.getRuntime().exec(command, null,absPathName );
            Thread.sleep(100);*/

            Process procTargetApp = JavaProcess.exec(DemoTargetApp.class, null);
            mProcessMap.put(DemoTargetApp.class.getSimpleName(), procTargetApp);
            return procTargetApp.isAlive();
        }catch (Exception ex){
            ex.printStackTrace();

        }
        return false;
    }

    @Override
    public void killTarget() {
            out.println("Killing Target");
            Process proc_DemoTargetApp = mProcessMap.get(DemoTargetApp.class.getSimpleName());
            proc_DemoTargetApp.destroyForcibly();
    }
}
