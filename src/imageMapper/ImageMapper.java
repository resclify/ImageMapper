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

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ImageMapper extends Application {
    private Pane stackPane;
    private ImageView imageView;

    private List<ImageArea> imageAreas = new ArrayList<>();
    private ImageArea markedImageArea = null;
    private TextField basePathText;
    private TextField filePathText;
    private TextArea htmlInputText;
    private TextArea htmlOutputText;
    private TextField coordsText;
    private TextArea titleText;
    private TextField altText;
    private TextArea contentText;
    private Button removeLineBreaksBtn;
    private Button removeWhitespacesBtn;
    private TextField onclickText;

    @Override
    public void start(Stage primaryStage) {
        stackPane = new Pane();
        imageView = new ImageView();
        stackPane.getChildren().add(imageView);
        stackPane.setOnMouseReleased(e -> updateFieldsForMarked());

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(stackPane, 0, 1, 4, 11);
        initControls(grid);

        primaryStage.setOnCloseRequest(e -> {
            saveProperties();
        });
        loadProperties();

        primaryStage.setTitle("ImageMapper");
        Scene scene = new Scene(grid, 1500, 768);
        grid.prefWidthProperty().bind(scene.widthProperty());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.show();
    }

    private void loadProperties() {
        Properties prop = new Properties();
        try (InputStream reader = new FileInputStream("ImageMapper.properties")) {
            prop.load(reader);
            basePathText.setText(prop.getProperty("basePath"));
            filePathText.setText(prop.getProperty("filePath"));
            htmlInputText.setText(prop.getProperty("outputHtmlText"));
            updateFieldsForMarked();
            updateMarkedFromFields();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveProperties() {
        Properties prop = new Properties();
        prop.setProperty("basePath", basePathText.getText());
        prop.setProperty("filePath", filePathText.getText());
        prop.setProperty("outputHtmlText", htmlOutputText.getText());
        try (OutputStream writer = new FileOutputStream("ImageMapper.properties")) {
            prop.store(writer, "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void areaClickedOrAdded(ImageArea clickedArea) {
        for (ImageArea i : imageAreas) {
            i.setMarked(false);
        }
        markedImageArea = clickedArea;
        markedImageArea.setMarked(true);
        stackPane.getChildren().removeAll(markedImageArea.getHandleCircles());
        stackPane.getChildren().addAll(markedImageArea.getHandleCircles());

        htmlInputText.setStyle("-fx-control-inner-background: white; -fx-text-fill: lightgrey; -fx-font-family: \"Courier New\";");
        htmlInputText.setEditable(false);
        htmlInputText.setTooltip(new Tooltip("Double click to unlock"));

        updateFieldsForMarked();
    }

    private void updateFieldsForMarked() {
        if (markedImageArea != null) {
            coordsText.setStyle("-fx-control-inner-background: white;");
            coordsText.setText(markedImageArea.getCoordsString());
            coordsText.setDisable(false);
            titleText.setText(markedImageArea.getTitle());
            titleText.setDisable(false);
            altText.setText(markedImageArea.getAlt());
            altText.setDisable(false);
            contentText.setText(markedImageArea.getDataContent());
            contentText.setDisable(false);
            removeLineBreaksBtn.setDisable(false);
            removeWhitespacesBtn.setDisable(false);
            onclickText.setText(markedImageArea.getOnClick());
            onclickText.setDisable(false);
        } else {
            coordsText.setDisable(true);
            titleText.setDisable(true);
            altText.setDisable(true);
            contentText.setDisable(true);
            removeLineBreaksBtn.setDisable(true);
            removeWhitespacesBtn.setDisable(true);
            onclickText.setDisable(true);
        }
        htmlOutputText.setText(HtmlWriter.write(imageAreas));
    }

    private void updateMarkedFromFields() {
        if (markedImageArea != null) {
            String[] coordinates = coordsText.getText().split("[,]");
            if (coordinates.length == 4) {
                try {
                    if (Double.parseDouble(coordinates[2]) - Double.parseDouble(coordinates[0]) > 0
                            && Double.parseDouble(coordinates[3]) - Double.parseDouble(coordinates[1]) > 0) {
                        markedImageArea.setX(Double.parseDouble(coordinates[0]));
                        markedImageArea.setY(Double.parseDouble(coordinates[1]));
                        markedImageArea.setWidth(Double.parseDouble(coordinates[2]) - Double.parseDouble(coordinates[0]));
                        markedImageArea.setHeight(Double.parseDouble(coordinates[3]) - Double.parseDouble(coordinates[1]));
                        coordsText.setStyle("-fx-control-inner-background: white;");
                    } else {
                        coordsText.setStyle("-fx-control-inner-background: orange;");
                    }
                } catch (Exception ex) {
                    coordsText.setStyle("-fx-control-inner-background: orange;");
                }
            } else {
                coordsText.setStyle("-fx-control-inner-background: orange;");
            }
            markedImageArea.setTitle(titleText.getText());
            markedImageArea.setAlt(altText.getText());
            markedImageArea.setDataContent(contentText.getText());
            markedImageArea.setOnClick(onclickText.getText());
        }
        htmlOutputText.setText(HtmlWriter.write(imageAreas));
    }

    private void initControls(GridPane grid) {
        Label basePathLabel = new Label("Base Path");
        basePathText = new TextField("C:/workspace/");
        basePathText.setPrefColumnCount(30);
        Label filePathLabel = new Label("File Path");
        filePathText = new TextField("Unbenannt.png");
        filePathText.setPrefColumnCount(30);
        Button loadImageBtn = new Button("Load Image");
        loadImageBtn.setOnAction(e -> loadImage());

        Label htmlInputLabel = new Label("HTML input");
        htmlInputText = new TextArea();
        htmlInputText.setPrefRowCount(6);
        htmlInputText.setWrapText(true);
        htmlInputText.textProperty().addListener((obs, oldText, newText) -> parseHtml(newText));
        htmlInputText.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-font-family: \"Courier New\";");
        htmlInputText.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 2) {
                htmlInputText.setEditable(true);
                htmlInputText.setTooltip(null);
                htmlInputText.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-font-family: \"Courier New\";");
            }
        });

        Label htmlOutputLabel = new Label("HTML output");
        htmlOutputText = new TextArea();
        htmlOutputText.setPrefRowCount(12);
        htmlOutputText.setWrapText(true);
        htmlOutputText.setEditable(false);
        htmlOutputText.setStyle("-fx-font-family: \"Courier New\";");

        Button newAreaBtn = new Button("Add new area");
        newAreaBtn.setOnMouseClicked(e ->
        {
            ImageArea area;
            if (markedImageArea != null) {
                if (e.isShiftDown()) {
                    area = new ImageArea(markedImageArea.getX(), markedImageArea.getY() + markedImageArea.getHeight() + 1, markedImageArea.getWidth(), markedImageArea.getHeight());
                } else {
                    area = new ImageArea(markedImageArea.getX() + markedImageArea.getWidth() + 1, markedImageArea.getY(), markedImageArea.getWidth(), markedImageArea.getHeight());
                }
            } else {
                area = new ImageArea(1, 1, 50, 50);
            }
            area.setOnMouseClicked(ev -> areaClickedOrAdded(area));
            for (Circle c : area.getHandleCircles()) {
                c.setOnMousePressed(ev -> areaClickedOrAdded(area));
            }
            stackPane.getChildren().add(1, area);
            imageAreas.add(area);
            areaClickedOrAdded(area);
        });
        Button deleteAreaBtn = new Button("Delete area");
        deleteAreaBtn.setOnAction(e ->
        {
            if (markedImageArea != null) {
                imageAreas.remove(markedImageArea);
                stackPane.getChildren().removeAll(markedImageArea.getHandleCircles());
                stackPane.getChildren().remove(markedImageArea);
                markedImageArea = null;
                updateFieldsForMarked();
            }
        });
        Button copyToClipBoard = new Button("Copy to clipboard");
        copyToClipBoard.setOnAction(e ->
        {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();

            content.putString(htmlOutputText.getText());
            clipboard.setContent(content);
        });

        Label coordsLabel = new Label("coords");
        coordsText = new TextField();
        coordsText.setStyle("-fx-font-family: \"Courier New\";");
        coordsText.setOnKeyReleased(e -> updateMarkedFromFields());
        Label titleLabel = new Label("title");
        titleText = new TextArea();
        titleText.setWrapText(true);
        titleText.setPrefRowCount(5);
        titleText.setStyle("-fx-font-family: \"Courier New\";");
        titleText.setOnKeyReleased(e -> updateMarkedFromFields());

        Label altLabel = new Label("alt");
        altText = new TextField();
        altText.setStyle("-fx-font-family: \"Courier New\";");
        altText.setOnKeyReleased(e -> updateMarkedFromFields());

        Label contentLabel = new Label("content-data");
        contentText = new TextArea();
        contentText.setWrapText(true);
        contentText.setPrefRowCount(20);
        contentText.setStyle("-fx-font-family: \"Courier New\";");
        contentText.setOnKeyReleased(e -> updateMarkedFromFields());

        removeWhitespacesBtn = new Button("Remove whitespaces");
        removeWhitespacesBtn.setOnAction(e -> {
            while (contentText.getText().contains("  ")) {
                contentText.setText(contentText.getText().replaceAll(" {2}", " ").trim());
            }
            updateMarkedFromFields();
        });
        removeLineBreaksBtn = new Button("Remove line breaks");
        removeLineBreaksBtn.setOnAction(e -> {
            while (contentText.getText().contains("\n")) {
                contentText.setText(contentText.getText().replaceAll(" \n", " "));
                contentText.setText(contentText.getText().replaceAll("\n", " "));
            }
            updateMarkedFromFields();
        });

        Label onclickLabel = new Label("onclick");
        onclickText = new TextField();
        onclickText.setStyle("-fx-font-family: \"Courier New\";");
        onclickText.setOnKeyReleased(e -> updateMarkedFromFields());

        Hyperlink infoText = new Hyperlink("https://www.github.com/resclify/ImageMapper");
        infoText.setAlignment(Pos.CENTER_RIGHT);
        infoText.setOnAction(e -> getHostServices().showDocument("https://www.github.com/resclify/ImageMapper"));

        grid.add(basePathLabel, 0, 0);
        grid.add(basePathText, 1, 0);
        grid.add(filePathLabel, 2, 0);
        grid.add(filePathText, 3, 0);
        grid.add(loadImageBtn, 4, 0, 1, 1);

        grid.add(infoText, 7, 0, 1, 1);
        GridPane.setHalignment(infoText, HPos.RIGHT);

        grid.add(htmlInputLabel, 4, 1, 3, 1);
        grid.add(htmlInputText, 5, 1, 3, 1);
        grid.add(htmlOutputLabel, 4, 2);
        grid.add(htmlOutputText, 5, 2, 3, 1);

        grid.add(newAreaBtn, 5, 3);
        grid.add(deleteAreaBtn, 6, 3);
        grid.add(copyToClipBoard, 7, 3);
        GridPane.setHalignment(copyToClipBoard, HPos.RIGHT);

        grid.add(coordsLabel, 4, 4);
        grid.add(coordsText, 5, 4, 3, 1);
        grid.add(titleLabel, 4, 5);
        grid.add(titleText, 5, 5, 3, 1);
        grid.add(altLabel, 4, 6);
        grid.add(altText, 5, 6, 3, 1);
        grid.add(contentLabel, 4, 7);
        grid.add(contentText, 5, 7, 3, 1);
        grid.add(removeWhitespacesBtn, 5, 8, 1, 1);
        grid.add(removeLineBreaksBtn, 6, 8, 1, 1);

        grid.add(onclickLabel, 4, 9);
        grid.add(onclickText, 5, 9, 3, 1);

    }

    private void loadImage() {
        try (InputStream inputStream = new FileInputStream(basePathText.getText() + "/" + filePathText.getText())) {
            Image newImg = new Image(inputStream);
            imageView.setImage(newImg);
        } catch (Exception ex) {
            ex.printStackTrace();
            imageView.setImage(null);
        }
    }

    private void parseHtml(String htmlString) {
        for (ImageArea area : imageAreas) {
            stackPane.getChildren().removeAll(area.getHandleCircles());
        }
        stackPane.getChildren().removeAll(imageAreas);
        stackPane.getChildren().clear();
        stackPane.getChildren().add(imageView);
        imageAreas.clear();
        try {
            HtmlReader.ParseResult parseResult = HtmlReader.read(htmlString, imageAreas);
            if (parseResult.getImgSrc() != null) {
                filePathText.setText(parseResult.getImgSrc());
                loadImage();
            }
            imageAreas = parseResult.getAreas();

            for (ImageArea area : imageAreas) {
                area.setOnMouseClicked(
                        e -> areaClickedOrAdded(area));
                for (Circle c : area.getHandleCircles()) {
                    c.setOnMousePressed(ev -> areaClickedOrAdded(area));
                }
                stackPane.getChildren().add(1, area);
            }
            htmlOutputText.setText(HtmlWriter.write(imageAreas));

            if (!imageAreas.isEmpty()) {
                markedImageArea = null;
                updateFieldsForMarked();
            }
        } catch (Exception e) {
            e.printStackTrace();
            htmlInputText.setStyle("-fx-control-inner-background: orange; -fx-font-family: \"Courier New\";");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
