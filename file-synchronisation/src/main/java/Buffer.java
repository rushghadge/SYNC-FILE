import java.util.ArrayList;
import java.util.List;

public class Buffer {
    public List<String> newFileNames = new ArrayList<>();
    public List<String> updateFileNames = new ArrayList<>();
    public List<String> fileUpdate = new ArrayList<>();
    public List<String> deleteFilenames = new ArrayList<>();
    public int clientId;

    public Buffer(int id) {
        this.clientId = id;
    }

    public String checkUpdate(){
        StringBuilder out = new StringBuilder();
        if(this.newFileNames.size()>0){
            out.append("ADD:");
            for (String file: this.newFileNames) {
                out.append(file).append("##");
            }
            out.append("STOP");
        }
        if(this.updateFileNames.size()>0){
            out.append("UPDATE:");
            for (int idx = 0; idx < this.updateFileNames.size(); idx++) {
                out.append(this.updateFileNames.get(idx)).append("#").append(this.fileUpdate.get(idx)).append("##");
            }
            out.append("STOP");
        }
        if(this.deleteFilenames.size()>0){
            out.append("DELETE:");
            for (String file: this.deleteFilenames) {
                out.append(file).append("##");
            }
            out.append("STOP");
        }
        return out.toString().equals("") ?
                        "None":
                        out.toString();
    }

    public void setNewFileNames(String newFileName) {
        this.newFileNames.add(newFileName);
    }

    public void setUpdateFileNames(String updateFileName, String update) {
        this.updateFileNames.add(updateFileName);
        this.fileUpdate.add(update);
    }

    public void setDeleteFilenames(String deleteFilename) {
        this.deleteFilenames.add(deleteFilename);
    }

    public void deleteNewFileNames(){
        this.newFileNames.clear();
    }

    public void deleteUpdateFileNames(){
        this.updateFileNames.clear();
        this.fileUpdate.clear();
    }

    public void deleteDeleteFilenames(){
        this.deleteFilenames.clear();
    }

}
