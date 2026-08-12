package br.edu.iffar.box.component;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.Map;

/**
 * Editor de texto rico (negrito, itálico, sublinhado, fonte, cor da fonte,
 * colar imagens) implementado do jeito mais nativo possível: um
 * contenteditable + document.execCommand(), sem nenhuma biblioteca JS
 * externa (Quill/TinyMCE/CKEditor/...). Componente nativo (UIInput, não
 * composite): integra com value/required/validação como qualquer input do
 * JSF, só a renderização é manual.
 *
 * O valor persistido é o HTML do conteúdo (inclusive imagens coladas, como
 * data URI embutida) — quem exibir esse valor precisa usar
 * escape="false" (ex.: h:outputText), já que ele contém marcação.
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:editor value="#{bean.objetivo}"/>
 */
@FacesComponent(
        value = Editor.COMPONENT_TYPE,
        createTag = true,
        tagName = "editor",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "editor.css", target = "head"),
        @ResourceDependency(library = "box", name = "editor.js", target = "head")
})
public class Editor extends UIInput {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Editor";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Editor";

    private static final String[] FONTES = {
            "Arial", "Georgia", "Times New Roman", "Courier New", "Verdana", "Trebuchet MS"
    };

    // Lista de permissão restrita às tags/atributos que o próprio editor
    // produz via execCommand (negrito/itálico/sublinhado/fonte/cor/imagem
    // colada). Tudo fora disso (script, on*, javascript:, iframe...) é
    // removido — inclusive se o POST for forjado sem passar pela UI.
    private static final Safelist SAFELIST = Safelist.none()
            .addTags("b", "i", "u", "font", "img", "div", "p", "br")
            .addAttributes("font", "color", "face")
            .addAttributes("img", "src")
            .addProtocols("img", "src", "data", "http", "https");

    private static String sanitizar(String html) {
        if (html == null) {
            return null;
        }
        Document.OutputSettings semFormatacao = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", SAFELIST, semFormatacao);
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
        Object valorAtual = getValue();
        String valor = valorAtual != null ? valorAtual.toString() : "";

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", "box-editor", null);

        encodeBarraDeFerramentas(writer);

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-editor-conteudo", null);
        writer.writeAttribute("contenteditable", "true", null);
        writer.write(valor);
        writer.endElement("div");

        // Campo real submetido com o form: o contenteditable em si não
        // participa da submissão nativa, o editor.js copia o innerHTML pra
        // cá a cada edição.
        writer.startElement("textarea", this);
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("class", "box-editor-valor", null);
        writer.write(valor);
        writer.endElement("textarea");

        writer.endElement("div");
    }

    private void encodeBarraDeFerramentas(ResponseWriter writer) throws IOException {
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-editor-barra", null);

        encodeBotaoComando(writer, "bold", "B", "Negrito");
        encodeBotaoComando(writer, "italic", "I", "Itálico");
        encodeBotaoComando(writer, "underline", "U", "Sublinhado");

        writer.startElement("select", this);
        writer.writeAttribute("class", "box-editor-fonte", null);
        writer.writeAttribute("data-box-editor-cmd", "fontName", null);
        writer.writeAttribute("title", "Fonte", null);
        for (String fonte : FONTES) {
            writer.startElement("option", this);
            writer.writeAttribute("value", fonte, null);
            writer.writeText(fonte, null);
            writer.endElement("option");
        }
        writer.endElement("select");

        writer.startElement("input", this);
        writer.writeAttribute("type", "color", null);
        writer.writeAttribute("class", "box-editor-cor", null);
        writer.writeAttribute("data-box-editor-cmd", "foreColor", null);
        writer.writeAttribute("title", "Cor da fonte", null);
        writer.writeAttribute("value", "#000000", null);
        writer.endElement("input");

        writer.endElement("div");
    }

    private void encodeBotaoComando(ResponseWriter writer, String comando, String rotulo, String titulo) throws IOException {
        writer.startElement("button", this);
        writer.writeAttribute("type", "button", null);
        writer.writeAttribute("class", "box-editor-btn box-editor-btn-" + comando, null);
        writer.writeAttribute("data-box-editor-cmd", comando, null);
        writer.writeAttribute("title", titulo, null);
        writer.writeText(rotulo, null);
        writer.endElement("button");
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parametros = context.getExternalContext().getRequestParameterMap();
        if (parametros.containsKey(clientId)) {
            setSubmittedValue(sanitizar(parametros.get(clientId)));
        }
    }
}
