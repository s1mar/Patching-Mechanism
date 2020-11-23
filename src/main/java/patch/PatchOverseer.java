package patch;

import demo.ITargetApp;
import orchestration.ISupervisor;
import org.beryx.textio.TextIoFactory;
import patch.entities.Delfile;
import patch.entities.Manifest;
import patch.entities.Modfile;
import patch.entities.Newfile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PatchOverseer extends UnicastRemoteObject implements IPatchOverseer_Target{

    private static volatile PatchOverseer mInstance;
    private ISupervisor remoteToSupervisor;
    private ITargetApp remoteToTarget;
    public static PatchOverseer getInstance() throws RemoteException {

        if(mInstance == null){
            mInstance = new PatchOverseer();
        }
        return mInstance;
    }


    protected PatchOverseer() throws RemoteException {
        super();
    }


    public static void main(String[] args){

        try {
            getInstance().initialize();
        }catch (Exception ex){
            println(ex.toString());
        }
    }


    private void initialize() throws Exception {

            println("/--------------------Patch Overseer-------------------------/");
            connectToSupervisor();
            startPatchingInterface();

    }

    private void startPatchingInterface(){
      try {
          connectToTarget();
          if(!runCommandBlock()){
              System.exit(0);
          }
      }
      catch (Exception ex){
              if (!(ex instanceof UnmarshalException)) {
                  println(ex.toString());
              }else{
                  startPatchingInterface();
              }

          }

    }

    private void connectToSupervisor() throws Exception {
        //Let's get a handle to the supervisor server
        Registry registry = LocateRegistry.getRegistry(ISupervisor.REGISTRY_PORT);
        remoteToSupervisor = (ISupervisor) registry.lookup(ISupervisor.serviceName);
        println("Connected to the Supervisor");
    }

    private void connectToTarget() throws Exception{
        //Let's get a handle to the target server
        Registry registry = LocateRegistry.getRegistry(ITargetApp.REGISTRY_PORT);
        remoteToTarget = (ITargetApp) registry.lookup(ITargetApp.SERVICE_NAME);
        println("Connected to the Target");

    }

    private boolean runCommandBlock() throws Exception{

        boolean promptRunning = true;
        while (promptRunning) {

            String command = TextIoFactory.getTextIO().newStringInputReader().read("Command>>");
            if (command.equals("initiate patch")) {
                remoteToTarget.patchingInitiated(this);
            } else if (command.equals("exit")) {
                promptRunning = false;
                println("Exited");
            } else {
                println("Invalid Command");
            }

        }
        return promptRunning;
    }

    private void processProcessPatch(){

        //Checking to see if the patch isn't malformed
        String relativePathToDirectory = Paths.get("").toAbsolutePath().toString();
        Path pathToPatchDirectory = Paths.get(relativePathToDirectory,"/patch/");
        Path pathToManifest = Paths.get(pathToPatchDirectory.toAbsolutePath().toString(),"manifest.json");
        boolean manifestExists = Files.exists(pathToManifest);

        if(!manifestExists){
            println("Malformed patch, manifest not found");
            return;
        }

        Manifest manifest = Manifest.readJsonFromPath(pathToManifest.toAbsolutePath().toString());


        boolean patchMalformed = false;
        //Verify the contents of the manifest with the patch files
        //New Files
        if(!manifest.getNewfiles().isEmpty()){

            for(Newfile nf : manifest.getNewfiles()){

                Path pathToNewfile = Paths.get(pathToPatchDirectory.toAbsolutePath().toString(),"/new/",nf.getName());
                if(!Files.exists(pathToNewfile)){
                    patchMalformed = true;
                    break;
                }
            }

        }



        if(!manifest.getModfiles().isEmpty() && !patchMalformed){

            for(Modfile mf : manifest.getModfiles()){
                Path pathToModfile = Paths.get(pathToPatchDirectory.toAbsolutePath().toString(),"/mod/",mf.getName());
                if(!Files.exists(pathToModfile)){
                    patchMalformed = true;
                    break;
                }

            }

        }

        //if the patch is malformed, quit now
        if(patchMalformed){
            println("\nPatch malformed, not applying it\n");
            return;
        }


        try {
            //else check the build numbers to verify that the patch is actually an update to the present build
            int buildNumberTarget = remoteToTarget.patchBuildNumber();
            if(buildNumberTarget>Integer.parseInt(manifest.getBuild())){
                println("\nTarget is already at a higher build number than the patch.");
                return;
            }

            //check the region
            if(!remoteToTarget.patchRegion().equals(manifest.getRegion())){
                    println("\nThis patch is not meant for this region. Quitting patching process");
                    return;

            }

        }
        catch (Exception ignored){}

        boolean isAColdPatch = false;
        if(manifest.isJustResources()){
            println("\nInitiating warm patching");
            println("\nNote: By default, in this process, the target doesn't need to be shutdown");

        }
        else {
            isAColdPatch = true;
            println("Initiating cold patching");
            println("Switching off the Target");
            try {
                remoteToTarget.patching_Shutdown();
            } catch (Exception ignored) {
            }

            println("Target killed");

        }


            println("Processing Patch");

            Path dirPath = Paths.get(relativePathToDirectory);
            patchingProcess(dirPath, pathToPatchDirectory, manifest);

            if(isAColdPatch) {
                try {
                    println("Restarting target\n");
                    remoteToSupervisor.startTarget();
                    Thread.sleep(100);
                    remoteToTarget = (ITargetApp) LocateRegistry.getRegistry(ITargetApp.REGISTRY_PORT).lookup(ITargetApp.SERVICE_NAME);

                } catch (Exception ex) {
                    println("Unable to bring the target online. Error : " + ex.toString());
                }
            }else{
                try {
                    remoteToTarget.patchHandoverComplete();
                }
                catch (Exception ex){
                    println("Unable to inform the target that the warm handover is complete");
                }
            }
    }


    private void patchingProcess(Path dirPath,Path pathToPatchDirectory,Manifest manifest){
        println(String.format("\n/-----------------------Patch build: %s--------------------------/",manifest.getBuild()));

        try {
            println("\n/---------Deleting Files---------/");
            if(manifest.getDelfiles().isEmpty()){
                println("No files to delete in this patch");
            }

            //Delete files
            for (Delfile delfile : manifest.getDelfiles()) {
                Path pathFile = Paths.get(dirPath.toAbsolutePath().toString(), delfile.getPath() + delfile.getName());
                println("File to be Deleted: "+pathFile.toString());
                boolean deleted = Files.deleteIfExists(pathFile);
                println("Was Deleted: "+deleted);
            }
        }catch (Exception ex){
            println("Error Del File: "+ ex.toString());
        }

        //Insert Modified files
        try{
            println("\n/---------Modifying Files---------/");
            if(manifest.getModfiles().isEmpty()){
                println("No files modified in this patch");
            }

            for(Modfile modfile : manifest.getModfiles()){

                Path pathSourceFile = Paths.get(pathToPatchDirectory.toAbsolutePath().toString(),"/mod/",modfile.getName());
                Path pathTargetFile = Paths.get(dirPath.toAbsolutePath().toString(), modfile.getPath(),modfile.getName());
                if(!Files.exists(pathTargetFile)) {
                    Files.createDirectories(Paths.get(pathTargetFile.toString()));
                }
                Path pathTarget = Files.copy(pathSourceFile, pathTargetFile,
                        StandardCopyOption.REPLACE_EXISTING);
                println("Target Modified: "+pathTarget.toString());
            }

        }catch (Exception ex){
            println("Error Mod File: "+ex.toString());
        }

        //Insert New Files
        try{
            println("\n/---------Inserting New Files---------/");
            if(manifest.getNewfiles().isEmpty()){
                println("No new files in this patch");
            }
            for(Newfile nf : manifest.getNewfiles()) {
                Path pathSourceFile = Paths.get(pathToPatchDirectory.toAbsolutePath().toString(), "/new/", nf.getName());
                Path pathTargetFile = Paths.get(dirPath.toAbsolutePath().toString(), nf.getPath(), nf.getName());

                if(!Files.exists(pathTargetFile)) {
                    Files.createDirectories(Paths.get(pathTargetFile.toString()));
                }
                Path pathTarget = Files.copy(pathSourceFile, pathTargetFile,
                        StandardCopyOption.REPLACE_EXISTING);
                println("Target Inserted: " + pathTarget.toString());
            }

        }catch (Exception ex){
            println("Error New File: "+ex.toString());
        }

        println("\n-----------------Patch Applied--------------------/\n");

    }

    private static void println(String s){

        System.out.println(s);
        TextIoFactory.getTextTerminal().println(s);

    }


    @Override
    public void patchInitiated(int targetState) {
            println("Target State :"+targetState);
            if(targetState == GO_AHEAD){
                    processProcessPatch();
            }
    }
}
