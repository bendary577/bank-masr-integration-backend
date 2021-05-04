package com.sun.supplierpoc.fileDelimiterExporters;

        import java.io.*;
        import java.nio.file.Files;
        import java.util.ArrayList;

public class GeneralExporterMethods {
    private String fileName;
    private StringBuilder fileContent = new StringBuilder();

    public GeneralExporterMethods() {
    }

    public GeneralExporterMethods(String fileName) {
        this.fileName = fileName;
    }

    public void generateSingleFile(PrintWriter printWriter, String path, String month, String FileName, boolean perLocation) throws IOException {
        File folder = new File(path + "/" + month);
        File generalSyncFile;

        String[] syncFileNames;
        BufferedReader reader;

        /*
         * Loop over all locations
         * */
        if (perLocation){
            String[] syncLocations = folder.list();
            if(syncLocations == null)
                syncLocations = new String[]{};

            for (String location : syncLocations) {
                if(new File(path + "/" + month, location).isDirectory()){
                    this.fileContent = new StringBuilder();
                    generalSyncFile = new File(path + "/" + location + "-" + FileName);
                    Files.deleteIfExists(generalSyncFile.toPath());

                    boolean status= generalSyncFile.getParentFile().mkdirs();
                    if(status)
                        generalSyncFile.createNewFile();

                    File locationFolder = new File(path  + "/" + month + "/" + location);
                    syncFileNames = locationFolder.list();

                    assert syncFileNames != null;
                    for (int i = 0; i < syncFileNames.length; i++) {
                        String syncFileName = syncFileNames[i];
                        reader = new BufferedReader(new FileReader(path  + "/" + month + "/"+ location + "/" + syncFileName));

                        String line;
                        String ls = System.getProperty("line.separator");
                        while ((line = reader.readLine()) != null) {
                            if(i != 0 && line.contains("VERSION")){
                                if(this.fileContent.charAt(this.fileContent.length()-1) != '\n'){
                                    if (this.fileContent.charAt(this.fileContent.length()-1) != '\r')
                                        this.fileContent.append(ls);
                                    else
                                        this.fileContent.append('\n');
                                }
                                continue;
                            }

                            this.fileContent.append(line);
                            this.fileContent.append(ls);
                        }

                        // delete the last new line separator
                        this.fileContent.deleteCharAt(this.fileContent.length() - 1);
                        this.fileContent.deleteCharAt(this.fileContent.length() - 2);

                        reader.close();
                    }
                    // delete the last new line separator
                    if(this.fileContent.length() > 0){
                        this.fileContent.deleteCharAt(this.fileContent.length() - 1);
                        this.fileContent.deleteCharAt(this.fileContent.length() - 2);
                    }

                    try (Writer writer = new BufferedWriter(new FileWriter(generalSyncFile))) {
                        writer.write(String.valueOf(this.fileContent));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        else{
            generalSyncFile = new File(path + "/" + FileName);
            Files.deleteIfExists(generalSyncFile.toPath());

            boolean status= generalSyncFile.getParentFile().mkdirs();
            if(status)
                generalSyncFile.createNewFile();

            syncFileNames = folder.list();
            if(syncFileNames == null)
                syncFileNames = new String[]{};

            String version = "";

            for (String pathname : syncFileNames) {

                if (new File(path, pathname).isDirectory())
                    continue;

                reader = new BufferedReader(new FileReader(path + "/" + month + "/" + pathname));

                String line;

                String ls = System.getProperty("line.separator");
                while ((line = reader.readLine()) != null) {
                    if (this.fileContent.length() > 0 && this.fileContent.charAt(this.fileContent.length() - 1) != '\n') {
                        if (this.fileContent.charAt(this.fileContent.length() - 1) != '\r')
                            this.fileContent.append(ls);
                        else
                            this.fileContent.append('\n');
                    }
                    if( !(line.length()<50) || !version.equals("1")){
                        this.fileContent.append(line);
                        this.fileContent.append(ls);}

                    version = "1";
                }

                // delete the last new line separator
                this.fileContent.deleteCharAt(this.fileContent.length() - 1);
                this.fileContent.deleteCharAt(this.fileContent.length() - 2);
                reader.close();
            }
            // delete the last new line separator
            if(this.fileContent.length() > 0){
                this.fileContent.deleteCharAt(this.fileContent.length() - 1);
                this.fileContent.deleteCharAt(this.fileContent.length() - 2);
            }

            try (Writer writer = new BufferedWriter(new FileWriter(generalSyncFile))) {
                writer.write(String.valueOf(this.fileContent));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (printWriter != null){
            printWriter.flush();
            printWriter.print(this.fileContent);
        }
    }

    public ArrayList<String> ListSyncFiles(String path){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> fileName = new ArrayList<>();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileName.add(path + "/" + listOfFile.getName());
            } else if (listOfFile.isDirectory()) {
                fileName.addAll(ListSyncFiles(path + "/" + listOfFile.getName()));
            }
        }
        return fileName;
    }

}
