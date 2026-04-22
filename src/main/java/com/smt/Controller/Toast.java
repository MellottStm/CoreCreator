package com.smt.Controller;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.log4j.Logger;

public class Toast {

    private static final Logger logger = Logger.getLogger(Toast.class);

    public static void makeText(Stage owner, String message, int durationMillis) {
        Stage toastStage = new Stage();
        toastStage.initOwner(owner);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 14px;");

        StackPane root = new StackPane(text);
        root.setStyle(
                "-fx-background-radius: 20; " +
                        "-fx-background-color: rgba(0, 0, 0, 0.85); " +
                        "-fx-padding: 14 28 14 28;"
        );
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        // 先显示一次，让 JavaFX 计算好宽高
        toastStage.show();
        toastStage.hide();   // 立即隐藏，后面再显示

        // 现在可以正确获取宽度了
        double toastWidth = root.getWidth();
        double toastHeight = root.getHeight();

        // 计算居中位置（在 owner 窗口底部上方）
        double x = owner.getX() + (owner.getWidth() - toastWidth) / 2;
        double y = owner.getY() + owner.getHeight() - toastHeight - 80; // 距离底部80像素

        // 防止超出屏幕
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        if (x < 0) x = 20;
        if (x + toastWidth > screenBounds.getWidth()) {
            x = screenBounds.getWidth() - toastWidth - 20;
        }
        if (y < 0) y = 20;

        toastStage.setX(x);
        toastStage.setY(y);

        toastStage.setAlwaysOnTop(true);
        toastStage.show();

        // 淡出动画
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(toastStage.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(durationMillis), new KeyValue(toastStage.opacityProperty(), 0.0))
        );

        timeline.setOnFinished(e -> toastStage.close());
        timeline.play();
    }

}
