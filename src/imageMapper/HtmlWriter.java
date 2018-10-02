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

import java.util.List;

public class HtmlWriter {

    public static String write(List<ImageArea> areas) {
        try {
            StringBuilder sb = new StringBuilder();
            for (ImageArea area : areas) {
                sb.append("<area ");
                sb.append("shape=\"rect\" ");
                sb.append("coords=\"").append(area.getCoordsString()).append("\" ");
                sb.append("alt=\"").append(escapeHtmlString(area.getAlt())).append("\" ");
                sb.append("data-toggle=\"popover\" ");
                sb.append("data-trigger=\"hover\" ");
                if (area.getTitle() != null && !"".equals(area.getTitle())) {
                    sb.append("title=\"").append(escapeHtmlString(area.getTitle())).append("\" ");
                }
                if (area.getOnClick() != null && !"".equals(area.getOnClick())) {
                    sb.append("onclick=\"").append(area.getOnClick()).append("\" ");
                }
                sb.append("data-content=\"").append(escapeHtmlString(area.getDataContent())).append("\" ");
                sb.append("/>\n");
            }

            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String escapeHtmlString(String s) {
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                //case '<': sb.append("&lt;"); break;
                //case '>': sb.append("&gt;"); break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case 'à':
                    sb.append("&agrave;");
                    break;
                case 'À':
                    sb.append("&Agrave;");
                    break;
                case 'â':
                    sb.append("&acirc;");
                    break;
                case 'Â':
                    sb.append("&Acirc;");
                    break;
                //case 'ä': sb.append("&auml;");break;
                //case 'Ä': sb.append("&Auml;");break;
                case 'å':
                    sb.append("&aring;");
                    break;
                case 'Å':
                    sb.append("&Aring;");
                    break;
                case 'æ':
                    sb.append("&aelig;");
                    break;
                case 'Æ':
                    sb.append("&AElig;");
                    break;
                case 'ç':
                    sb.append("&ccedil;");
                    break;
                case 'Ç':
                    sb.append("&Ccedil;");
                    break;
                case 'é':
                    sb.append("&eacute;");
                    break;
                case 'É':
                    sb.append("&Eacute;");
                    break;
                case 'è':
                    sb.append("&egrave;");
                    break;
                case 'È':
                    sb.append("&Egrave;");
                    break;
                case 'ê':
                    sb.append("&ecirc;");
                    break;
                case 'Ê':
                    sb.append("&Ecirc;");
                    break;
                case 'ë':
                    sb.append("&euml;");
                    break;
                case 'Ë':
                    sb.append("&Euml;");
                    break;
                case 'ï':
                    sb.append("&iuml;");
                    break;
                case 'Ï':
                    sb.append("&Iuml;");
                    break;
                case 'ô':
                    sb.append("&ocirc;");
                    break;
                case 'Ô':
                    sb.append("&Ocirc;");
                    break;
                //case 'ö': sb.append("&ouml;");break;
                //case 'Ö': sb.append("&Ouml;");break;
                case 'ø':
                    sb.append("&oslash;");
                    break;
                case 'Ø':
                    sb.append("&Oslash;");
                    break;
                case 'ß':
                    sb.append("&szlig;");
                    break;
                case 'ù':
                    sb.append("&ugrave;");
                    break;
                case 'Ù':
                    sb.append("&Ugrave;");
                    break;
                case 'û':
                    sb.append("&ucirc;");
                    break;
                case 'Û':
                    sb.append("&Ucirc;");
                    break;
                //case 'ü': sb.append("&uuml;");break;
                //case 'Ü': sb.append("&Uuml;");break;
                case '®':
                    sb.append("&reg;");
                    break;
                case '©':
                    sb.append("&copy;");
                    break;
                case '€':
                    sb.append("&euro;");
                    break;
                //case ' ': sb.append("&nbsp;");break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString().replaceAll(" {2}", "&nbsp;&nbsp;");
    }
}
