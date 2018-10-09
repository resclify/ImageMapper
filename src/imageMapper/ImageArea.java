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

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ImageArea extends Rectangle {

    private final double HANDLE_RADIUS = 5;
    private final Color HANDLE_COLOR = new Color(1.0f, 0.5f, 0.5f, 1.0);
    private final Color HANDLE_COLOR_MARK = Color.RED;
    private final Color MOVER_HANDLE_COLOR = new Color(0.5f, 0.5f, 1, 1.0);
    private final Color MOVER_HANDLE_COLOR_MARK = new Color(0, 0, 1, 1.0);

    private final Color DEFAULT_FILL_COLOR = new Color(1, 0, 0, 0.2);
    private final Color MOUSE_OVER_COLOR = new Color(0, 0, 1, 0.3);
    private final Color MARK_COLOR = new Color(0, 1, 0, 0.2);

    private Circle resizeHandleNW;
    private Circle resizeHandleSE;
    private Circle moveHandle;

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
        this.setStrokeType(StrokeType.OUTSIDE);
        this.getStrokeDashArray().addAll(8.0, 13.0, 8.0, 13.0);

        makeDraggable();

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
        for (Circle c : getHandleCircles()) {
            c.setOnMouseEntered(e -> {
                if (!marked) {
                    this.setFill(MOUSE_OVER_COLOR);

                }
            });
            c.setOnMouseExited(e -> {
                if (!marked) {
                    this.setFill(DEFAULT_FILL_COLOR);

                }
            });
        }
    }

    private void makeDraggable() {
        // top left resize handle:
        resizeHandleNW = new Circle(HANDLE_RADIUS, HANDLE_COLOR);
        // bind to top left corner of Rectangle:
        resizeHandleNW.centerXProperty().bind(this.xProperty());
        resizeHandleNW.centerYProperty().bind(this.yProperty());

        // bottom right resize handle:
        resizeHandleSE = new Circle(HANDLE_RADIUS, HANDLE_COLOR);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.centerXProperty().bind(this.xProperty().add(this.widthProperty()));
        resizeHandleSE.centerYProperty().bind(this.yProperty().add(this.heightProperty()));

        // move handle:
        moveHandle = new Circle(HANDLE_RADIUS, MOVER_HANDLE_COLOR);
        // bind to bottom center of Rectangle:
        moveHandle.centerXProperty().bind(this.xProperty().add(this.widthProperty().divide(2)));
        moveHandle.centerYProperty().bind(this.yProperty().add(this.heightProperty()));

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

                if (event.isControlDown()) {
                    for (Node n : this.getParent().getChildrenUnmodifiable()) {
                        if (n instanceof ImageArea && !n.equals(this)) {
                            Rectangle r = new Rectangle(this.getX() + deltaX + 1, this.getY() + 1, this.getWidth() - deltaX - 2, this.getHeight() - 2);
                            if ((r.intersects(n.getBoundsInLocal()))) {
                                deltaX = 0;
                            }
                            r = new Rectangle(this.getX() + 1, this.getY() + deltaY + 1, this.getWidth() - 2, this.getHeight() - deltaY - 2);
                            if ((r.intersects(n.getBoundsInLocal()))) {
                                deltaY = 0;
                            }
                        }
                    }
                }

                if (event.isShiftDown()) {
                    deltaX *= 0.23;
                    deltaY *= 0.23;
                }

                double newX = this.getX() + deltaX;
                if (newX > 0
                        && newX <= this.getX() + this.getWidth()) {
                    this.setX(newX);
                    this.setWidth(this.getWidth() - deltaX);
                }
                double newY = this.getY() + deltaY;
                if (newY > 0
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

                if (event.isControlDown()) {
                    for (Node n : this.getParent().getChildrenUnmodifiable()) {
                        if (n instanceof ImageArea && !n.equals(this)) {
                            Rectangle r = new Rectangle(this.getX() + 1, this.getY() + 1, this.getWidth() + deltaX - 2, this.getHeight() - 2);
                            if ((r.intersects(n.getBoundsInLocal()))) {
                                deltaX = 0;
                            }
                            r = new Rectangle(this.getX() + 1, this.getY() + 1, this.getWidth() - 2, this.getHeight() + deltaY - 2);
                            if ((r.intersects(n.getBoundsInLocal()))) {
                                deltaY = 0;
                            }
                        }
                    }
                }

                if (event.isShiftDown()) {
                    deltaX *= 0.23;
                    deltaY *= 0.23;
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

                if (event.isControlDown()) {
                    for (Node n : this.getParent().getChildrenUnmodifiable()) {
                        if (n instanceof ImageArea && !n.equals(this)) {
                            Rectangle r = new Rectangle(this.getX() + deltaX + 1, this.getY() + 1, this.getWidth() - 2, this.getHeight() - 2);
                            if ((r.intersects(n.getBoundsInLocal()))) {
                                deltaX = 0;
                            }
                            r = new Rectangle(this.getX() + 1, this.getY() + deltaY + 1, this.getWidth() - 2, this.getHeight() - 2);
                            if ((r.intersects(n.getBoundsInLocal()))) {
                                deltaY = 0;
                            }
                        }
                    }
                }

                if (event.isShiftDown()) {
                    deltaX *= 0.23;
                    deltaY *= 0.23;
                }

                double newX = this.getX() + deltaX;
                double newMaxX = newX + this.getWidth();
                if (newX > 0 && newMaxX <= this.getParent().getBoundsInLocal().getWidth()) {
                    this.setX(newX);
                }
                double newY = this.getY() + deltaY;
                double newMaxY = newY + this.getHeight();
                if (newY > 0 && newMaxY <= this.getParent().getBoundsInLocal().getHeight()) {
                    this.setY(newY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });
    }

    private void setUpDragging(Circle circle, Wrapper<Point2D> mouseLocation) {

        circle.setOnDragDetected(event -> {
            circle.getParent().setCursor(Cursor.NONE);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        circle.setOnMouseReleased(event -> {
            circle.getParent().setCursor(Cursor.DEFAULT);
            this.setX(Math.round(getX()));
            this.setY(Math.round(getY()));
            this.setWidth(Math.round(getWidth()));
            this.setHeight(Math.round(getHeight()));

            //Set mouse position to circle
            Platform.runLater(() -> {
                try {
                    Robot robot = new Robot();
                    Point2D point2D = this.localToScreen(circle.getCenterX(), circle.getCenterY());
                    robot.mouseMove((int) Math.round(point2D.getX()), (int) Math.round(point2D.getY()));
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            });
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
            resizeHandleNW.setFill(HANDLE_COLOR_MARK);
            resizeHandleSE.setFill(HANDLE_COLOR_MARK);
            moveHandle.setFill(MOVER_HANDLE_COLOR_MARK);
        } else {
            this.setFill(DEFAULT_FILL_COLOR);
            resizeHandleNW.setFill(HANDLE_COLOR);
            resizeHandleSE.setFill(HANDLE_COLOR);
            moveHandle.setFill(MOVER_HANDLE_COLOR);
        }
    }

    public List<Circle> getHandleCircles() {
        return Arrays.asList(resizeHandleNW, moveHandle, resizeHandleSE);
    }
}
