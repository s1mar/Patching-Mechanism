
package patch.entities;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

public class Manifest {

    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("build")
    @Expose
    private String build;
    @SerializedName("newfiles")
    @Expose
    private List<Newfile> newfiles = null;
    @SerializedName("modfiles")
    @Expose
    private List<Modfile> modfiles = null;
    @SerializedName("delfiles")
    @Expose
    private List<Delfile> delfiles = null;

    @SerializedName("justres")
    @Expose
    private boolean justResources;
    public boolean isJustResources() {
        return justResources;
    }

    public void setJustResources(boolean justResources) {
        this.justResources = justResources;
    }
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public List<Newfile> getNewfiles() {
        return newfiles;
    }

    public void setNewfiles(List<Newfile> newfiles) {
        this.newfiles = newfiles;
    }

    public List<Modfile> getModfiles() {
        return modfiles;
    }

    public void setModfiles(List<Modfile> modfiles) {
        this.modfiles = modfiles;
    }

    public List<Delfile> getDelfiles() {
        return delfiles;
    }

    public void setDelfiles(List<Delfile> delfiles) {
        this.delfiles = delfiles;
    }

    public static Manifest readJsonFromPath(String absPath){

    Manifest manifest = null;
        try {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(absPath));

            // convert JSON to Object
            manifest = new Gson().fromJson(reader, new TypeToken<Manifest>() {}.getType());

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    return manifest;

    }

}
