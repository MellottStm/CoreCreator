package com.smt.MCP;

import com.smt.Editor.EditorManager;
import com.smt.LangChain.Bean.ContentBean;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MCPManager {

    private static String TAG = "FilesManager";

    public final static Logger logger = Logger.getLogger(TAG);



    public static void managerProject (String path, String content, ContentBean.OperationType type) {
        switch (type) {
            case add:
            case update:
                updateFile(path,content);
                break;
            case del:
                delFile(path);
                break;
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
            if (path.endsWith(".docx")) {
                logger.info("这是docx文件类型!");
                EditorManager.writeDocx(file,content);
            } else {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                Files.createDirectories(Paths.get(path).getParent());
                Files.writeString(Paths.get(path), content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("操作文件失败:" + e);
        }
    }



}
