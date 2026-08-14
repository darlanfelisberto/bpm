package br.edu.iffar.box.component.editor;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rich text editor (bold, italic, underline, font, font color, pasting
 * images), using Quill (self-hosted under vendor/quill/, no CDN) as the
 * editing engine. Native component (UIInput, not a composite): integrates
 * with value/required/validation like any JSF input, only the rendering
 * is manual.
 *
 * The persisted value is the content's HTML (including pasted images, as
 * an embedded data URI) — whoever displays this value needs to use
 * escape="false" (e.g. h:outputText), since it contains markup.
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:editor value="#{bean.objective}"/>
 */
@FacesComponent(
        value = Editor.COMPONENT_TYPE,
        createTag = true,
        tagName = "editor",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "vendor/quill/quill.snow.css", target = "head"),
        @ResourceDependency(library = "box", name = "editor/editor.css", target = "head"),
        @ResourceDependency(library = "box", name = "vendor/quill/quill.js", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "editor/editor.js", target = "head")
})
public class Editor extends UIInput {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Editor";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Editor";

    // Allowlist for the HTML Quill produces (via quill.getSemanticHTML(),
    // not quill.root.innerHTML — avoids the editor's internal markers like
    // <span class="ql-ui" contenteditable="false"> that don't make sense
    // outside of it) for the formats enabled in the toolbar: headers,
    // font, size, bold/italic/underline/strikethrough, color/background,
    // sub/superscript, blockquote, code, lists, indent, alignment,
    // direction, link, image and clear formatting. Video was left out of
    // the toolbar (see editor.js) because it would produce an
    // <iframe src="..."> with a free-form URL — same problem as "style",
    // it would be outside this allowlist's control. Anything not listed
    // here (script, on*, javascript:, iframe...) is stripped, even if the
    // POST is forged without going through the UI.
    private static final Safelist SAFELIST = Safelist.none()
            .addTags("p", "br", "h1", "h2", "h3", "blockquote", "pre",
                    "ol", "ul", "li", "sub", "sup", "s", "strong", "em", "u", "span", "img", "a")
            .addAttributes("p", "class")
            .addAttributes("li", "class")
            .addAttributes("h1", "class").addAttributes("h2", "class").addAttributes("h3", "class")
            .addAttributes("blockquote", "class")
            .addAttributes("pre", "data-language")
            .addAttributes("span", "class", "style")
            .addAttributes("strong", "style")
            .addAttributes("em", "style")
            .addAttributes("u", "style")
            .addAttributes("s", "style")
            .addAttributes("img", "src")
            .addAttributes("a", "href")
            .addProtocols("img", "src", "data", "http", "https")
            .addProtocols("a", "href", "http", "https", "mailto");

    // Quill stores font color/background as an inline style (style="color:
    // ..."/"background-color: ..."). Jsoup doesn't validate the style
    // attribute's content by itself, only whether it's present — without
    // this check a forged POST could inject arbitrary CSS (url(), for
    // instance, allows exfiltrating data). Only passes if it's exactly one
    // of these two declarations, with the color in #hex or rgb(...).
    private static final Pattern SAFE_STYLE = Pattern.compile(
            "(color|background-color):\\s*(#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6}|rgb\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*\\))\\s*;?\\s*");

    // Package-private (not private) on purpose: allows a direct unit test
    // (EditorSanitizeTest, same package) without reflection.
    static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        Document.OutputSettings noFormatting = new Document.OutputSettings().prettyPrint(false);
        String clean = Jsoup.clean(html, "", SAFELIST, noFormatting);
        Document doc = Jsoup.parseBodyFragment(clean);
        doc.outputSettings(noFormatting);
        for (Element element : doc.select("[style]")) {
            if (!SAFE_STYLE.matcher(element.attr("style").trim()).matches()) {
                element.removeAttr("style");
            }
        }
        // <a> rel/target don't come from Jsoup (they're not in the
        // allowlist) — set here, always with the same safe value, instead
        // of trusting whatever came in the POST.
        for (Element link : doc.select("a[href]")) {
            link.attr("target", "_blank");
            link.attr("rel", "noopener noreferrer");
        }
        return doc.body().html();
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);
        Object currentValue = getValue();
        String value = currentValue != null ? currentValue.toString() : "";

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", "box-editor", null);

        // Quill takes over this div (toolbar + editable area); it reads
        // the HTML already present here as the initial content.
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-editor-quill", null);
        writer.write(value);
        writer.endElement("div");

        // Actual field submitted with the form: Quill itself doesn't
        // take part in native submission, editor.js copies the generated
        // HTML here on every change (text-change event).
        writer.startElement("textarea", this);
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("class", "box-editor-value", null);
        writer.write(value);
        writer.endElement("textarea");

        writer.endElement("div");
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
        if (parameters.containsKey(clientId)) {
            setSubmittedValue(sanitize(parameters.get(clientId)));
        }
    }
}
