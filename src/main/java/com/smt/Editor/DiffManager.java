package com.smt.Editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiffManager {




    /**
     * 比较两个文本内容，返回带有差异标记的行列表
     */
    public static List<Diff> compareTexts(List<String> original, List<String> modified) {
        List<Diff> diffTagList = new ArrayList<>();
        if (modified.size() > original.size()) {
            for (int i = 0;i < modified.size();i++) {
                if ((i >= original.size()) || (original.get(i).isEmpty() && !modified.get(i).isEmpty())) {
                    Diff diff;
                    if (i >= original.size()) {
                        diff = new Diff(Diff.DiffTag.INSERT, null, modified.get(i));
                    } else {
                        diff = new Diff(Diff.DiffTag.INSERT, original.get(i), modified.get(i));
                    }
                    diffTagList.add(diff);
                } else if (modified.get(i).equals(original.get(i))) {
                    Diff diff = new Diff(Diff.DiffTag.EQUAL,original.get(i),modified.get(i));
                    diffTagList.add(diff);
                } else if (!original.get(i).isEmpty() && modified.get(i).isEmpty()) {
                    Diff diff = new Diff(Diff.DiffTag.DEL,original.get(i),modified.get(i));
                    diffTagList.add(diff);
                } else if (!(original.get(i).equals(modified.get(i)))) {
                    Diff diff = new Diff(Diff.DiffTag.CHANGE,original.get(i),modified.get(i));
                    diffTagList.add(diff);
                }
            }
        } else {
            for (int i = 0;i < original.size();i++) {
                if ((i >= modified.size())|| (!original.get(i).isEmpty() && modified.get(i).isEmpty())) {
                    Diff diff;
                    if (i >= modified.size()) {
                        diff = new Diff(Diff.DiffTag.DEL, original.get(i), null);
                    } else {
                        diff = new Diff(Diff.DiffTag.DEL, original.get(i), modified.get(i));
                    }
                    diffTagList.add(diff);
                } else if (modified.get(i).equals(original.get(i))) {
                    Diff diff = new Diff(Diff.DiffTag.EQUAL,original.get(i),modified.get(i));
                    diffTagList.add(diff);
                } else if (original.get(i).isEmpty() && !modified.get(i).isEmpty()) {
                    Diff diff = new Diff(Diff.DiffTag.INSERT,original.get(i),modified.get(i));
                    diffTagList.add(diff);
                } else if (!(original.get(i).equals(modified.get(i)))) {
                    Diff diff = new Diff(Diff.DiffTag.CHANGE,original.get(i),modified.get(i));
                    diffTagList.add(diff);
                }
            }
        }
        // 3. 生成差异行
        return diffTagList;
    }

    // 一个简单的测试方法
    public static void main(String[] args) {

        List<String> original = Arrays.asList("这是一行测试代码。", "这是第二行。", "这是第三行。","","这是第五行");
        List<String> modified = Arrays.asList("这是一行修改后的代码。", "这是第二行。","","这是第四行");
        List<Diff> list = compareTexts(original,modified);
        for (Diff diff:list) {
           System.out.println("原来的行:" + diff.originalValue);
           System.out.println("更改的行:" + diff.modifiedValue);
           System.out.println("更改的类型:" + diff.tag);
        }

    }

}
