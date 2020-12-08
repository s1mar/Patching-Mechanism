# Patching System
The project is currently in the prototype state. In this project, I've demonstrated a patching mechanism that supports both:
- **WARM_PATCH** : The resources that the target process relies on are patched and the target notified. The target isn't killed or restarted.
- **COLD_PATCH** : The core target process itself is patched and thus has to be restarted. 

### New Features:
 - Instead of pasting the patch contents, now the patch comes as a zip file, just put the zip file in the patch folder.
 - Uses Checksums to validate whether a patch package has been corrupted or not.

### Demo!
 The Demo is compromised of 4 cases. Please open the project in an IDE of your choice and run the Supervisor class.
- **Case 1 : Warm Patch**: In this case, the resources that the target process relies on are patched and the target stays online, which means **0 downtime**.
    - Restore the resources to the pre-patch state by replacing the **work** directory with the one in **work.zip** 
    - copy the patch.zip from `./patch cases/case 1- Warm Handover/patch` and place in `./patch/`
    - Run the Supervisor and enter this command in the Patch Overseer Window `initiate patch`
    - As you can see in the image below, the target resumed job with the updated resources it got from the patch. It was never killed.
![alt text](https://raw.githubusercontent.com/s1mar/Patching-Mechanism/main/pics/case1_postpatch.jpg?raw=true)

- **Case 2 : Cold Patch**: In this case, the running process itself has undergone some modifications and needs to shutdown and run again.
    - Restore the resources to the pre-patch state by replacing the **work** directory with the one in **work.zip** 
    - Copy the patch.zip from `./patch cases/case 2 - Cold Reboot/patch` and place in `./patch/`
    - Run the Supervisor and enter this command in the Patch Overseer Window `initiate patch`
    - As you can see in the image below, the target had to shutdown, patched and then restarted, it's now at v2.0.
    ![alt text](https://raw.githubusercontent.com/s1mar/Patching-Mechanism/main/pics/case2_postpatch.jpg?raw=true)
- **Case 3 : Region Doesn't Match**: In this case, the target procecss is running in the region **''us-east-1''** and the patch is meant for the region **''India''**
    - Restore the resources to the pre-patch state by replacing the **work** directory with the one in **work.zip** 
    - copy the patch.zip from `./patch cases/case 3 - Region Doesn't match/patch` and place in `./patch/`
    - Run the Supervisor and enter this command in the Patch Overseer Window `initiate patch`
    - As you can see in the image below, the patch is not applied since it's not meant for this regions. The target procecss is running in the region **''us-east-1''** and the patch is meant for the region **''India''**
![alt text](https://raw.githubusercontent.com/s1mar/Patching-Mechanism/main/pics/case3.jpg?raw=true)
- **Case 4 : Build Greater**: In this case, the running process is already at a higher build number than the patch, basically the patch which we're pushing is old.
    - Restore the resources to the pre-patch state by replacing the **work** directory with the one in **work.zip** 
    - copy the patch.zip from `./patch cases/case 4 - Build Greater/patch` and place in `./patch/`
    - Run the Supervisor and enter this command in the Patch Overseer Window `initiate patch`
    - As you can see in the image below, the patch is not applied since the target is already at a higher build.
![alt text](https://raw.githubusercontent.com/s1mar/Patching-Mechanism/main/pics/case4.jpg?raw=true)
