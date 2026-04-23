package com.smt.LangChain;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ToolsPrompt {

    public static String TAG = "ToolsPrompt";

    public final static Logger logger = Logger.getLogger(TAG);


    public static String LLMPrompt = "你是一个AI助手,你需要读取用户项目文件中的所有文件内容然后基于这些内容和用户的query完成用户的请求\n" +
            "#核心任务\n" +
            "一、读取用户项目文档的所有内容\n" +
            "二、基于项目文档的内容完成用户的请求\n" +
            "示例：\n" +
            "读取用户的项目目录内容如下:\n" +
            "文件路径为:F:\\ATest\\ATest\\src\\main\\java\\com\\smt\\Main.java的文件内容为:\n" +
            "package com.smt;\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "\n" +
            "    }\n" +
            "}\n" +
            "用户的请求如下：\n" +
            "帮我写一个快速排序算法。\n" +
            "输出的内容如下：\n" +
            "package com.smt; // 根据你的项目结构，可能需要调整包名\n" +
            "import java.util.Arrays;\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        // 1. 准备测试数据\n" +
            "        int[] arr = {64, 34, 25, 12, 22, 11, 90};\n" +
            "        System.out.println(\"排序前: \" + Arrays.toString(arr));\n" +
            "        // 2. 调用快速排序\n" +
            "        quickSort(arr, 0, arr.length - 1);\n" +
            "        // 3. 输出结果\n" +
            "        System.out.println(\"排序后: \" + Arrays.toString(arr));\n" +
            "    }\n" +
            "    /**\n" +
            "     * 快速排序主方法\n" +
            "     * @param arr 待排序数组\n" +
            "     * @param low 起始索引\n" +
            "     * @param high 结束索引\n" +
            "     */\n" +
            "    public static void quickSort(int[] arr, int low, int high) {\n" +
            "        if (low < high) {\n" +
            "            // 1. 找到分区点（pivot），此时 pivot 左边的数都比它小，右边的数都比它大\n" +
            "            int pi = partition(arr, low, high);\n" +
            "            // 2. 递归排序左半部分\n" +
            "            quickSort(arr, low, pi - 1);\n" +
            "            // 3. 递归排序右半部分\n" +
            "            quickSort(arr, pi + 1, high);\n" +
            "        }\n" +
            "    }\n" +
            "    /**\n" +
            "     * 分区操作：选取最后一个元素为基准，将小于基准的数移到左边，大于基准的数移到右边\n" +
            "     */\n" +
            "    private static int partition(int[] arr, int low, int high) {\n" +
            "        int pivot = arr[high]; // 选取最右边的元素作为基准\n" +
            "        int i = (low - 1); // 较小元素的索引（指向比 pivot 小的区域的最后一个位置）\n" +
            "        for (int j = low; j < high; j++) {\n" +
            "            // 如果当前元素小于或等于基准\n" +
            "            if (arr[j] <= pivot) {\n" +
            "                i++;\n" +
            "                // 交换 arr[i] 和 arr[j]\n" +
            "                int temp = arr[i];\n" +
            "                arr[i] = arr[j];\n" +
            "                arr[j] = temp;\n" +
            "            }\n" +
            "        }\n" +
            "        // 最后将基准元素放到正确的位置（i+1）\n" +
            "        int temp = arr[i + 1];\n" +
            "        arr[i + 1] = arr[high];\n" +
            "        arr[high] = temp;\n" +
            "        return i + 1; // 返回基准元素的索引\n" +
            "    }\n" +
            "}\n" +
            "更改的文件路径是F:\\ATest\\ATest\\src\\main\\java\\com\\smt\\Main.java\n" +
            "更改类型是update\n" +
            "三、输出的内容必须满足用户的请求\n" +
            "四、输出更改的文件完整路径，该路径必须在用户的项目根路径内。\n" +
            "五、更改的类型有add、del、update、none，这些字段的规则如下，必须严格遵守规则进行输出\n" +
            "1、add表示新增的文件\n" +
            "2、del表示删除的文件\n" +
            "3、update表示更改的文件\n" +
            "4、如果没有任何文件更改则输出none\n" +
            "六、输出格式（必须严格JSON）\n" +
            "只允许输出以下的结构，上面示例的输出：\n" +
            "{\n" +
            "  \"result\": [\n" +
            "    {\n" +
            "      \"content\": \"package com.smt; // 根据你的项目结构，可能需要调整包名\\nimport java.util.Arrays;\\npublic class Main {\\n    public static void main(String[] args) {\\n        // 1. 准备测试数据\\n        int[] arr = {64, 34, 25, 12, 22, 11, 90};\\n        System.out.println(\\\"排序前: \\\" + Arrays.toString(arr));\\n        // 2. 调用快速排序\\n        quickSort(arr, 0, arr.length - 1);\\n        // 3. 输出结果\\n        System.out.println(\\\"排序后: \\\" + Arrays.toString(arr));\\n    }\\n    /**\\n     * 快速排序主方法\\n     * @param arr 待排序数组\\n     * @param low 起始索引\\n     * @param high 结束索引\\n     */\\n    public static void quickSort(int[] arr, int low, int high) {\\n        if (low < high) {\\n            // 1. 找到分区点（pivot），此时 pivot 左边的数都比它小，右边的数都比它大\\n            int pi = partition(arr, low, high);\\n            // 2. 递归排序左半部分\\n            quickSort(arr, low, pi - 1);\\n            // 3. 递归排序右半部分\\n            quickSort(arr, pi + 1, high);\\n        }\\n    }\\n    /**\\n     * 分区操作：选取最后一个元素为基准，将小于基准的数移到左边，大于基准的数移到右边\\n     */\\n    private static int partition(int[] arr, int low, int high) {\\n        int pivot = arr[high]; // 选取最右边的元素作为基准\\n        int i = (low - 1); // 较小元素的索引（指向比 pivot 小的区域的最后一个位置）\\n        for (int j = low; j < high; j++) {\\n            // 如果当前元素小于或等于基准\\n            if (arr[j] <= pivot) {\\n                i++;\\n                // 交换 arr[i] 和 arr[j]\\n                int temp = arr[i];\\n                arr[i] = arr[j];\\n                arr[j] = temp;\\n            }\\n        }\\n        // 最后将基准元素放到正确的位置（i+1）\\n        int temp = arr[i + 1];\\n        arr[i + 1] = arr[high];\\n        arr[high] = temp;\\n        return i + 1; // 返回基准元素的索引\\n    }\\n}\",\n" +
            "      \"path\": \"F:\\\\ATest\\\\ATest\\\\src\\\\main\\\\java\\\\com\\\\smt\\\\Main.java\",\n" +
            "      \"type\": \"update\",\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "\n";


    public static String getFilePathAndContentPrompt(String dir) {
        // 👉 输入目录路径
        Path startPath = Paths.get(dir);
        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            logger.info("路径不存在或不是目录！");
            return null;
        }
        StringBuffer result = new StringBuffer();
        logger.info("用户的项目根目录是:" + dir);
        result.append("用户的项目根目录是:").append(dir).append("\n");
        result.append("读取用户的项目目录所有的文件内容如下:\n");
        try {
            Stream<Path> paths = Files.walk(startPath);
            paths.filter(Files::isRegularFile) // 只处理文件
            .forEach(path -> {
                try {
                    // 读取文件内容（默认 UTF-8）
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    result.append("文件路径为:").append(path.toAbsolutePath()).append("的文件内容为:\n").append(content).append("\n");
                } catch (IOException e) {
                    logger.info("读取失败: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.warn("读取文件失败:" + e);
        }
        logger.info(result.toString());
        return result.toString();
    }


}
