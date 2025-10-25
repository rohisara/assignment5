
package paintapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class simplepaintapp extends Application {
    private Canvas canvas = new Canvas(900, 600);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private DrawingTool tool = new FreehandTool();
    private Color color = Color.BLACK;
    private double size = 5;

    @Override
    public void start(Stage stage) {
        clearCanvas();
        setupMouseEvents();

        ColorPicker cp = new ColorPicker(color);
        cp.setOnAction(e -> color = cp.getValue());

        Slider slider = new Slider(1, 50, size);
        Label sVal = new Label(String.format("%.0f", size));
        slider.valueProperty().addListener((o, v, n) -> {
            size = n.doubleValue();
            sVal.setText(String.format("%.0f", size));
        });

        Button clear = new Button("Clear");
        clear.setOnAction(e -> clearCanvas());

        Button[] btns = {
            makeToolBtn("Freehand", new FreehandTool()),
            makeToolBtn("Line", new LineTool()),
            makeToolBtn("Rect", new RectangleTool()),
            makeToolBtn("Circle", new CircleTool()),
            makeToolBtn("Eraser", new EraserTool())
        };

        HBox bar = new HBox(10);
        bar.getChildren().addAll(btns);
        bar.getChildren().addAll(new Label("Color:"), cp, new Label("Size:"), slider, sVal, clear);
        bar.setStyle("-fx-padding:10; -fx-background-color:#eee;");

        BorderPane root = new BorderPane(canvas, bar, null, null, null);
        stage.setScene(new Scene(root, 920, 700));
        stage.setTitle("Simple Paint App");
        stage.show();
    }

    private Button makeToolBtn(String name, DrawingTool t) {
        Button b = new Button(name);
        b.setOnAction(e -> tool = t);
        return b;
    }

    private void setupMouseEvents() {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> tool.pressed(gc, e.getX(), e.getY(), color, size));
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> tool.dragged(gc, e.getX(), e.getY(), color, size));
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> tool.released(gc, e.getX(), e.getY(), color, size));
    }

    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void main(String[] a) { launch(a); }
}

// Base class
abstract class DrawingTool {
    double startX, startY;
    javafx.scene.image.WritableImage snap;
    abstract void pressed(GraphicsContext gc, double x, double y, Color c, double s);
    abstract void dragged(GraphicsContext gc, double x, double y, Color c, double s);
    abstract void released(GraphicsContext gc, double x, double y, Color c, double s);
}

// Freehand
class FreehandTool extends DrawingTool {
    public void pressed(GraphicsContext g, double x, double y, Color c, double s) { draw(g, x, y, c, s); }
    public void dragged(GraphicsContext g, double x, double y, Color c, double s) { draw(g, x, y, c, s); }
    public void released(GraphicsContext g, double x, double y, Color c, double s) {}
    void draw(GraphicsContext g, double x, double y, Color c, double s) {
        g.setFill(c); g.fillOval(x - s / 2, y - s / 2, s, s);
    }
}

// Line
class LineTool extends DrawingTool {
    public void pressed(GraphicsContext g, double x, double y, Color c, double s) { startX = x; startY = y; snap = g.getCanvas().snapshot(null, null); }
    public void dragged(GraphicsContext g, double x, double y, Color c, double s) { redraw(g, c, s, x, y); }
    public void released(GraphicsContext g, double x, double y, Color c, double s) { redraw(g, c, s, x, y); snap = null; }
    void redraw(GraphicsContext g, Color c, double s, double x, double y) {
        g.drawImage(snap, 0, 0); g.setStroke(c); g.setLineWidth(s); g.strokeLine(startX, startY, x, y);
    }
}

// Rectangle
class RectangleTool extends DrawingTool {
    public void pressed(GraphicsContext g, double x, double y, Color c, double s) { startX = x; startY = y; snap = g.getCanvas().snapshot(null, null); }
    public void dragged(GraphicsContext g, double x, double y, Color c, double s) { draw(g, c, s, x, y); }
    public void released(GraphicsContext g, double x, double y, Color c, double s) { draw(g, c, s, x, y); snap = null; }
    void draw(GraphicsContext g, Color c, double s, double x, double y) {
        g.drawImage(snap, 0, 0); g.setStroke(c); g.setLineWidth(s);
        g.strokeRect(startX, startY, x - startX, y - startY);
    }
}

// Circle
class CircleTool extends DrawingTool {
    public void pressed(GraphicsContext g, double x, double y, Color c, double s) { startX = x; startY = y; snap = g.getCanvas().snapshot(null, null); }
    public void dragged(GraphicsContext g, double x, double y, Color c, double s) { draw(g, c, s, x, y); }
    public void released(GraphicsContext g, double x, double y, Color c, double s) { draw(g, c, s, x, y); snap = null; }
    void draw(GraphicsContext g, Color c, double s, double x, double y) {
        g.drawImage(snap, 0, 0); g.setStroke(c); g.setLineWidth(s);
        g.strokeOval(Math.min(startX, x), Math.min(startY, y), Math.abs(x - startX), Math.abs(y - startY));
    }
}

// Eraser
class EraserTool extends DrawingTool {
    public void pressed(GraphicsContext g, double x, double y, Color c, double s) { draw(g, x, y, s); }
    public void dragged(GraphicsContext g, double x, double y, Color c, double s) { draw(g, x, y, s); }
    public void released(GraphicsContext g, double x, double y, Color c, double s) {}
    void draw(GraphicsContext g, double x, double y, double s) {
        g.setFill(Color.WHITE); g.fillOval(x - s / 2, y - s / 2, s, s);
    }
}
