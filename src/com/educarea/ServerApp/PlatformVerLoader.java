package com.educarea.ServerApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import transfers.VersionList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlatformVerLoader {

    public static VersionList loadFromFile(String filePath) throws IOException {
        Logger log = Logger.getLogger(EducLogger.class.getName());
        VersionList list = null;
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String data = "";
            while (reader.ready()){
                data+=reader.readLine()+"\n";
            }
            ObjectMapper mapper = new ObjectMapper();
            list = mapper.readValue(data, VersionList.class);
        }catch (Exception e){
            log.log(Level.SEVERE, "can't load file with version info", e);
            throw e;
        }
        return list;
    }
}
