package com.smt.Cache;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smt.Controller.Toast;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CacheManager {

    private static String TAG = "CacheManager";

    public final static Logger logger = Logger.getLogger(TAG);

    private static final String CACHE_FILE = "./cache/app_data.json";

    public static void saveConfig (String API_KEY,String LLM_NAME,String LLM_URL) {
        try {
            if (API_KEY.isEmpty() || LLM_NAME.isEmpty() || LLM_URL.isEmpty()) {
                return;
            }
            Configure.API_KEY = API_KEY;
            Configure.LLM_NAME = LLM_NAME;
            Configure.LLM_URL = LLM_URL;

            // 更新数据
            // 确保目录存在
            File file = new File(CACHE_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            JSONObject saveJson = loadCache();
            // 写入 JSON
            if (saveJson == null) {
                saveJson = new JSONObject();
            }
            saveJson.put("API_KEY", Configure.API_KEY);
            saveJson.put("LLM_NAME", Configure.LLM_NAME);
            saveJson.put("LLM_URL", Configure.LLM_URL);
            Path path = Paths.get(CACHE_FILE);
            Files.createDirectories(path.getParent());
            Files.writeString(path, saveJson.toJSONString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    public static void saveProjectPath (String projectPath) {
        try {
            // 更新数据
            // 确保目录存在
            File file = new File(CACHE_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            JSONObject saveJson = loadCache();
            // 写入 JSON
            if (saveJson == null) {
                saveJson = new JSONObject();
            }
            saveJson.put("project_path", projectPath);
            Path path = Paths.get(CACHE_FILE);
            Files.createDirectories(path.getParent());
            Files.writeString(path, saveJson.toJSONString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn(e);
        }
    }


    public static void savePathList (ObservableList<File> projectList) {
        try {
            // 更新数据
            // 确保目录存在
            File file = new File(CACHE_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            JSONObject saveJson = loadCache();
            // 写入 JSON
            if (saveJson == null) {
                saveJson = new JSONObject();
            }
            JSONArray pathArray = new JSONArray();
            for (File projectDir:projectList) {
                JSONObject dirJson = new JSONObject();
                dirJson.put("path",projectDir.getAbsolutePath());
                pathArray.add(dirJson);
            }
            saveJson.put("pathList",pathArray);
            Path path = Paths.get(CACHE_FILE);
            Files.createDirectories(path.getParent());
            Files.writeString(path, saveJson.toJSONString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn(e);
        }
    }



    /** 从 JSON 文件加载缓存 */
    public static JSONObject loadCache() {
        JSONObject loadJson = null;
        try {
            File file = new File(CACHE_FILE);
            if (!file.exists()) {
                logger.info("缓存文件不存在，将使用默认值");
                return null;
            }
            String content = Files.readString(Paths.get(CACHE_FILE));
            if (!content.isEmpty()) {
                loadJson = JSONObject.parseObject(content);
                logger.info("加载获取的缓存内容:" + loadJson.toJSONString());
            }
        } catch (Exception ex) {
            logger.warn(ex);
        }
        return loadJson;
    }


}
