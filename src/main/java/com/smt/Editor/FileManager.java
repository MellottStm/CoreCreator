package com.smt.Editor;

import com.smt.Controller.MainController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

public class FileManager {

    public static File createFile(File dir, String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        File file = new File(dir, name);

        if (file.exists()) {
            throw new RuntimeException("文件已存在");
        }

        if (!file.createNewFile()) {
            throw new RuntimeException("创建失败");
        }

        return file;
    }

    public static File createDirectory(File dir, String name) {
        File folder = new File(dir, name);

        if (folder.exists()) {
            throw new RuntimeException("文件夹已存在");
        }

        if (!folder.mkdirs()) {
            throw new RuntimeException("创建失败");
        }

        return folder;
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.delete()) {
                throw new RuntimeException("删除失败");
            }
        }
    }

    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                delete(f);
            }
        }
        dir.delete();
    }

    public static void rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);

        if (newFile.exists()) {
            throw new RuntimeException("目标已存在");
        }

        if (!file.renameTo(newFile)) {
            throw new RuntimeException("重命名失败");
        }
    }


    public static void refresh(TreeView<File> treeView, File rootDir, Set<String> expandedPaths) {
        TreeItem<File> root = buildNode(rootDir);
        treeView.setRoot(root);
        restoreExpanded(root, expandedPaths);
    }

    private static TreeItem<File> buildNode(File file) {
        TreeItem<File> item = new TreeItem<>(file);

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                Arrays.sort(children, (a, b) -> {
                    if (a.isDirectory() != b.isDirectory()) {
                        return a.isDirectory() ? -1 : 1;
                    }
                    return a.getName().compareToIgnoreCase(b.getName());
                });

                for (File child : children) {
                    item.getChildren().add(buildNode(child));
                }
            }
        }

        return item;
    }

    private static void restoreExpanded(TreeItem<File> item, Set<String> expanded) {
        if (expanded.contains(item.getValue().getAbsolutePath())) {
            item.setExpanded(true);
        }

        for (TreeItem<File> child : item.getChildren()) {
            restoreExpanded(child, expanded);
        }
    }


    public static ContextMenu create(File file, MainController controller) {
        ContextMenu menu = new ContextMenu();

        if (file.isDirectory()) {

            MenuItem newFile = new MenuItem("New File");
            newFile.setOnAction(e -> controller.handleCreateFile(file));

            MenuItem newFolder = new MenuItem("New Directory");
            newFolder.setOnAction(e -> controller.handleCreateFolder(file));

            menu.getItems().addAll(newFile, newFolder);

        }

        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> controller.handleRename(file));

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(e -> controller.handleDelete(file));

        menu.getItems().addAll(rename, delete);

        return menu;
    }

}
