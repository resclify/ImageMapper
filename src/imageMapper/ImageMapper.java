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
    private TextField onclickText;

    @Override
    public void start(Stage primaryStage) {
        stackPane = new Pane();
        imageView = new ImageView();
        stackPane.getChildren().add(imageView);
        stackPane.setOnMouseReleased(e -> updateDisplayForMarked());

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(stackPane, 0, 1, 4, 10);
        initControls(grid);


        primaryStage.setOnCloseRequest(e -> {
            saveProperties();
        });
        loadProperties();

        primaryStage.setTitle("ImageMapper");
        Scene scene = new Scene(grid, 1500, 768);
        grid.prefWidthProperty().bind(scene.widthProperty());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadProperties() {
        Properties prop = new Properties();
        try (InputStream reader = new FileInputStream("ImageMapper.properties")) {
            prop.load(reader);
            basePathText.setText(prop.getProperty("basePath"));
            filePathText.setText(prop.getProperty("filePath"));
            htmlInputText.setText(prop.getProperty("outputHtmlText"));
            updateDisplayForMarked();
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

    private void areaClicked(ImageArea clickedArea) {
        for (ImageArea i : imageAreas) {
            i.setMarked(false);
        }
        markedImageArea = clickedArea;
        markedImageArea.setMarked(true);

        updateDisplayForMarked();
    }

    private void updateDisplayForMarked() {
        if (markedImageArea != null) {
            coordsText.setStyle("-fx-control-inner-background: white;");
            coordsText.setText(markedImageArea.getCoordsString());
            titleText.setText(markedImageArea.getTitle());
            altText.setText(markedImageArea.getAlt());
            contentText.setText(markedImageArea.getDataContent());
            onclickText.setText(markedImageArea.getOnClick());
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
        loadImageBtn.setOnAction(e -> {
            try (InputStream inputStream = new FileInputStream(basePathText.getText() + "/" + filePathText.getText())) {
                Image newImg = new Image(inputStream);
                imageView.setImage(newImg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Label htmlInputLabel = new Label("HTML input");
        htmlInputText = new TextArea();
        htmlInputText.setPrefRowCount(4);
        htmlInputText.setWrapText(true);
        htmlInputText.textProperty().addListener((obs, oldText, newText) -> parseHtml(newText));

        Label htmlOutputLabel = new Label("HTML output");
        htmlOutputText = new TextArea();
        htmlOutputText.setPrefRowCount(10);
        htmlOutputText.setWrapText(true);
        htmlOutputText.setEditable(false);
        htmlOutputText.setStyle("-fx-font-family: \"Courier New\";");

        Button newAreaBtn = new Button("New Area");
        newAreaBtn.setOnAction(e ->
        {
            ImageArea area = new ImageArea(100, 10, 100, 100, "", "", "", "");
            area.setOnMouseClicked(ev -> areaClicked(area));
            stackPane.getChildren().add(1, area);
            imageAreas.add(area);
            updateDisplayForMarked();
        });
        Button deleteAreaBtn = new Button("Delete Area");
        deleteAreaBtn.setOnAction(e ->
        {
            if (markedImageArea != null) {
                imageAreas.remove(markedImageArea);
                stackPane.getChildren().removeAll(markedImageArea.getHandleCircles());
                stackPane.getChildren().remove(markedImageArea);
                markedImageArea = null;
                updateDisplayForMarked();
            }
        });
        Button copyToClipBoard = new Button("Copy to Clipboard");
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
        titleText.setPrefRowCount(3);
        titleText.setStyle("-fx-font-family: \"Courier New\";");
        titleText.setOnKeyReleased(e -> updateMarkedFromFields());

        Label altLabel = new Label("alt");
        altText = new TextField();
        altText.setStyle("-fx-font-family: \"Courier New\";");
        altText.setOnKeyReleased(e -> updateMarkedFromFields());

        Label contentLabel = new Label("content-data");
        contentText = new TextArea();
        contentText.setWrapText(true);
        contentText.setStyle("-fx-font-family: \"Courier New\";");
        contentText.setOnKeyReleased(e -> updateMarkedFromFields());

        Label onclickLabel = new Label("onclick");
        onclickText = new TextField();
        onclickText.setStyle("-fx-font-family: \"Courier New\";");
        onclickText.setOnKeyReleased(e -> updateMarkedFromFields());

        Hyperlink infoText = new Hyperlink("https://www.github.com/resclify/ImageMapper");
        infoText.setPrefWidth(500);
        infoText.setAlignment(Pos.CENTER_RIGHT);
        infoText.setOnAction(e -> getHostServices().showDocument("https://www.github.com/resclify/ImageMapper"));

        grid.add(basePathLabel, 0, 0);
        grid.add(basePathText, 1, 0);
        grid.add(filePathLabel, 2, 0);
        grid.add(filePathText, 3, 0);
        grid.add(loadImageBtn, 4, 0, 1, 1);

        grid.add(infoText, 5, 0, 3, 1);
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
        grid.add(onclickLabel, 4, 8);
        grid.add(onclickText, 5, 8, 3, 1);

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
            }
            imageAreas = parseResult.getAreas();

            for (ImageArea area : imageAreas) {
                area.setOnMouseClicked(
                        e -> areaClicked(area));
                stackPane.getChildren().add(1, area);
            }
            htmlOutputText.setText(HtmlWriter.write(imageAreas));
            htmlInputText.setStyle("-fx-control-inner-background: green; -fx-font-family: \"Courier New\";");
            markedImageArea = null;
        } catch (Exception e) {
            e.printStackTrace();
            htmlInputText.setStyle("-fx-control-inner-background: orange; -fx-font-family: \"Courier New\";");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
