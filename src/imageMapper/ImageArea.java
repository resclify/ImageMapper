/*
 * Copyright 2018 resclify
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package imageMapper;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageArea extends Rectangle {

    private final double HANDLE_RADIUS = 5;
    private final Color HANDLE_COLOR = Color.RED;
    private final Color MOVER_HANDLE_COLOR = new Color(0, 0, 1, 1.0);
    private final Color DEFAULT_FILL_COLOR = new Color(1, 0, 0, 0.2);
    private final Color MOUSE_OVER_COLOR = new Color(0, 0, 1, 0.3);
    private final Color MARK_COLOR = new Color(0, 1, 0, 0.2);

    private List<Circle> handleCircles = new ArrayList<>();

    private String title;
    private String alt;
    private String dataContent;
    private String onClick;
    private boolean marked;

    public ImageArea(double x, double y, double width, double height) {
        this(x, y, width, height, "", "", "", "");
    }

    public ImageArea(double x, double y, double width, double height, String title, String alt, String dataContent, String onClick) {
        super(x, y, width, height);
        this.title = title;
        this.alt = alt;
        this.dataContent = dataContent;
        this.onClick = onClick;
        this.marked = false;

        this.setFill(DEFAULT_FILL_COLOR);
        this.setStroke(Color.RED);
        this.setStrokeWidth(1);
        this.setStrokeType(StrokeType.INSIDE);
        this.getStrokeDashArray().addAll(12.0, 17.0, 12.0, 17.0);
        this.setOnMouseEntered(e -> {
            if (!marked) {
                this.setFill(MOUSE_OVER_COLOR);
            }
        });
        this.setOnMouseExited(e -> {
            if (!marked) {
                this.setFill(DEFAULT_FILL_COLOR);
            }
        });
        makeDraggable();
    }

    private void makeDraggable() {
        // top left resize handle:
        Circle resizeHandleNW = new Circle(HANDLE_RADIUS, HANDLE_COLOR);
        // bind to top left corner of Rectangle:
        resizeHandleNW.centerXProperty().bind(this.xProperty());
        resizeHandleNW.centerYProperty().bind(this.yProperty());

        // bottom right resize handle:
        Circle resizeHandleSE = new Circle(HANDLE_RADIUS, HANDLE_COLOR);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.centerXProperty().bind(this.xProperty().add(this.widthProperty()));
        resizeHandleSE.centerYProperty().bind(this.yProperty().add(this.heightProperty()));

        // move handle:
        Circle moveHandle = new Circle(HANDLE_RADIUS, MOVER_HANDLE_COLOR);
        // bind to bottom center of Rectangle:
        moveHandle.centerXProperty().bind(this.xProperty().add(this.widthProperty().divide(2)));
        moveHandle.centerYProperty().bind(this.yProperty().add(this.heightProperty()));

        handleCircles.add(resizeHandleNW);
        handleCircles.add(resizeHandleSE);
        handleCircles.add(moveHandle);

        // force circles to live in same parent as rectangle:
        this.parentProperty().addListener((obs, oldParent, newParent) -> {
            for (Circle c : Arrays.asList(moveHandle, resizeHandleNW, resizeHandleSE)) {
                Pane currentParent = (Pane) c.getParent();
                if (currentParent != null) {
                    //currentParent.getChildren().remove(c);
                }
                if (newParent != null) {
                    ((Pane) newParent).getChildren().add(c);
                }
            }
        });

        Wrapper<Point2D> mouseLocation = new Wrapper<>();

        setUpDragging(resizeHandleNW, mouseLocation);
        setUpDragging(resizeHandleSE, mouseLocation);
        setUpDragging(moveHandle, mouseLocation);

        resizeHandleNW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();

                if (event.isShiftDown()) {
                    deltaX *= 0.25;
                    deltaY *= 0.25;
                }

                if (event.isControlDown()) {
                    deltaY = 0;
                } else if (event.isAltDown()) {
                    deltaX = 0;
                }

                double newX = this.getX() + deltaX;
                if (newX >= 0
                        && newX <= this.getX() + this.getWidth()) {
                    this.setX(newX);
                    this.setWidth(this.getWidth() - deltaX);
                }
                double newY = this.getY() + deltaY;
                if (newY >= 0
                        && newY <= this.getY() + this.getHeight()) {
                    this.setY(newY);
                    this.setHeight(this.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleSE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();

                if (event.isShiftDown()) {
                    deltaX *= 0.25;
                    deltaY *= 0.25;
                }
                if (event.isControlDown()) {
                    deltaY = 0;
                } else if (event.isAltDown()) {
                    deltaX = 0;
                }

                double newMaxX = this.getX() + this.getWidth() + deltaX;
                if (newMaxX >= this.getX()
                        && newMaxX <= this.getParent().getBoundsInLocal().getWidth()) {
                    this.setWidth(this.getWidth() + deltaX);
                }
                double newMaxY = this.getY() + this.getHeight() + deltaY;
                if (newMaxY >= this.getY()
                        && newMaxY <= this.getParent().getBoundsInLocal().getHeight()) {
                    this.setHeight(this.getHeight() + deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        moveHandle.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();

                if (event.isShiftDown()) {
                    deltaX *= 0.25;
                    deltaY *= 0.25;
                }

                double newX = this.getX() + deltaX;
                double newMaxX = newX + this.getWidth();
                if (newX >= 0 && newMaxX <= this.getParent().getBoundsInLocal().getWidth()) {
                    this.setX(newX);
                }
                double newY = this.getY() + deltaY;
                double newMaxY = newY + this.getHeight();
                if (newY >= 0 && newMaxY <= this.getParent().getBoundsInLocal().getHeight()) {
                    this.setY(newY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });
    }

    private void setUpDragging(Circle circle, Wrapper<Point2D> mouseLocation) {

        circle.setOnDragDetected(event -> {
            circle.getParent().setCursor(Cursor.CLOSED_HAND);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        circle.setOnMouseReleased(event -> {
            circle.getParent().setCursor(Cursor.DEFAULT);
            this.setX(Math.round(getX()));
            this.setY(Math.round(getY()));
            this.setWidth(Math.round(getWidth()));
            this.setHeight(Math.round(getHeight()));
            mouseLocation.value = null;
        });
    }

    static class Wrapper<T> {
        T value;
    }


    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlt() {
        return alt != null ? alt : "";
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getDataContent() {
        return dataContent != null ? dataContent : "";
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }

    public String getOnClick() {
        return onClick != null ? onClick : "";
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    public boolean isMarked() {
        return marked;
    }

    public String getCoordsString() {
        return String.format("%.0f,%.0f,%.0f,%.0f", this.getX(), this.getY(),
                this.getX() + this.getWidth(), this.getY() + this.getHeight());
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
        if (marked) {
            this.setFill(MARK_COLOR);
        } else {
            this.setFill(DEFAULT_FILL_COLOR);
        }
    }

    public List<Circle> getHandleCircles() {
        return handleCircles;
    }
}
