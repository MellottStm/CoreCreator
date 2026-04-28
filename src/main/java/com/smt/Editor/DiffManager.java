package com.smt.Editor;

import difflib.DiffRow;
import difflib.DiffRowGenerator;

import java.util.Arrays;
import java.util.List;

public class DiffManager {

    /**
     * 比较两个文本内容，返回带有差异标记的行列表
     */
    public static List<DiffRow> compareTexts(List<String> original, List<String> revised) {
        // 1. 使用 Builder 模式构建 DiffRowGenerator
        // 这是唯一正确的初始化方式
        DiffRowGenerator generator = new DiffRowGenerator.Builder()
                .showInlineDiffs(true)       // 开启行内差异（单词级高亮）
                .build();

        // 3. 生成差异行
        return generator.generateDiffRows(original, revised);
    }

    // 一个简单的测试方法
    public static void main(String[] args) {

        List<String> original = Arrays.asList("这是一行测试代码。", "这是第二行。", "这是最后一行。");
        List<String> revised = Arrays.asList("这是一行修改后的代码。", "这是第二行。","");

        List<DiffRow> diffRows = compareTexts(original, revised);

        // 打印结果，查看 DiffRow 的结构
        for (DiffRow row : diffRows) {
            System.out.println("旧行: " + row.getOldLine());
            System.out.println("新行: " + row.getNewLine());
            System.out.println("行类型: " + row.getTag()); // EQUAL, INSERT, DELETE, CHANGE
            System.out.println("---");
        }
    }

}
