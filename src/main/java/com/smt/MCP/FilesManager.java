package com.smt.MCP;

import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesManager {

    private static String TAG = "FilesManager";

    public final static Logger logger = Logger.getLogger(TAG);

    public enum FileOperationType {

        add("add"),

        del("del"),

        update("update"),

        none("none");

        public String value;

        FileOperationType(String value) {
            this.value = value;
        }

        public static FileOperationType getType (String value) {
            for (FileOperationType type:FileOperationType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return none;
        }

    }


    public static void managerProject (String path,String content,String type) {
        FileOperationType opt = FileOperationType.getType(type);
        switch (opt) {
            case add:
                addFile(path,content);
                break;
            case del:
                delFile(path);
                break;
            case update:
                updateFile(path,content);
                break;
        }
    }


    private static void addFile (String path,String content) {
        try {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Files.createDirectories(Paths.get(path).getParent());
            Files.writeString(Paths.get(path), content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("操作文件失败:" + e);
        }
    }

    private static void delFile (String path) {
        try {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                return;
            }
            Files.delete(Paths.get(path));
        } catch (Exception e) {
            logger.warn("操作文件失败:" + e);
        }
    }

    private static void updateFile (String path,String content) {
        try {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Files.createDirectories(Paths.get(path).getParent());
            Files.writeString(Paths.get(path), content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("操作文件失败:" + e);
        }
    }



}
