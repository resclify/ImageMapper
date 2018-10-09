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

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HtmlReader {
    public static class ParseResult {
        private String imgSrc;
        private List<ImageArea> areas;

        public ParseResult(String imgSrc, List<ImageArea> areas) {
            this.imgSrc = imgSrc;
            this.areas = areas;
        }

        public String getImgSrc() {
            return imgSrc;
        }

        public List<ImageArea> getAreas() {
            return areas;
        }
    }

    public static ParseResult read(String input, List<ImageArea> areas) throws Exception {
        MapParser mapParser = new MapParser();
        try (InputStream stream = new ByteArrayInputStream(input.getBytes())) {
            ParserDelegator pd = new ParserDelegator();
            pd.parse(new BufferedReader(new InputStreamReader(stream)), mapParser, false);
            if (mapParser.thrownException) {
                throw new Exception("Exception occured during parsing.");
            } else {
                return new ParseResult(mapParser.imgSrc, mapParser.areas);
            }
        }
    }

    private static class MapParser extends HTMLEditorKit.ParserCallback {
        List<ImageArea> areas = new ArrayList<>();
        String imgSrc = null;
        boolean thrownException = false;

        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            handleSimpleTag(t, a, 0);
        }

        @Override
        public void handleEndTag(HTML.Tag t, int pos) {
        }

        @Override
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (t.equals(HTML.Tag.IMG)) {
                imgSrc = (String) a.getAttribute(Attribute.SRC);
            }
            if (t.equals(HTML.Tag.AREA)) {
                if (!"rect".equals(a.getAttribute(Attribute.SHAPE))) {
                    thrownException = true;
                }

                String coords = (String) a.getAttribute(Attribute.COORDS);
                String title = (String) a.getAttribute(Attribute.TITLE);
                String alt = (String) a.getAttribute(Attribute.ALT);
                String dataContent = (String) a.getAttribute("data-content");
                String onclick = (String) a.getAttribute("onclick");

                String[] coordinates = coords.split("[,]");
                if (coordinates.length == 4) {
                    try {
                        double x = Double.parseDouble(coordinates[0]);
                        double y = Double.parseDouble(coordinates[1]);
                        double width = Double.parseDouble(coordinates[2]) - Double.parseDouble(coordinates[0]);
                        double height = Double.parseDouble(coordinates[3]) - Double.parseDouble(coordinates[1]);

                        if (width < 0) {
                            x = x + width;
                            width = -width;
                        }
                        if (height < 0) {
                            y = y + height;
                            height = -height;
                        }
                        if (x <= 0)
                            x = 1;
                        if (y <= 0)
                            y = 1;
                        ImageArea newImageArea = new ImageArea(x, y, width, height, title, alt, dataContent, onclick);
                        areas.add(newImageArea);

                    } catch (Exception ex) {
                        thrownException = true;
                    }
                }
            }
        }
    }
}