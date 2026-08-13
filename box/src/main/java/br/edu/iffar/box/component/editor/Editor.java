package br.edu.iffar.box.component;

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
 * Editor de texto rico (negrito, itálico, sublinhado, fonte, cor da fonte,
 * colar imagens), usando Quill (auto-hospedado em vendor/quill/, sem CDN)
 * como motor de edição. Componente nativo (UIInput, não composite): integra
 * com value/required/validação como qualquer input do JSF, só a
 * renderização é manual.
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
        @ResourceDependency(library = "box", name = "vendor/quill/quill.snow.css", target = "head"),
        @ResourceDependency(library = "box", name = "editor.css", target = "head"),
        @ResourceDependency(library = "box", name = "vendor/quill/quill.js", target = "head"),
        @ResourceDependency(library = "box", name = "box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "editor.js", target = "head")
})
public class Editor extends UIInput {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Editor";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Editor";

    // Lista de permissão com o HTML que o Quill produz (via
    // quill.getSemanticHTML(), não quill.root.innerHTML — evita marcadores
    // internos do editor tipo <span class="ql-ui" contenteditable="false">
    // que não fazem sentido fora dele) para os formatos habilitados na
    // toolbar: cabeçalhos, fonte, tamanho, negrito/itálico/sublinhado/
    // tachado, cor/fundo, sub/sobrescrito, citação, código, listas, recuo,
    // alinhamento, direção, link, imagem e limpar formatação. Vídeo foi
    // deixado de fora da toolbar (ver editor.js) porque geraria um
    // <iframe src="..."> com URL livre — igual a "style", ficaria fora do
    // controle desta lista de permissão. Tudo que não está aqui (script,
    // on*, javascript:, iframe...) é removido, inclusive se o POST for
    // forjado sem passar pela UI.
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

    // O Quill guarda cor/fundo da fonte como estilo inline (style="color:
    // ..."/"background-color: ..."). Jsoup não valida o conteúdo do
    // atributo style sozinho, só a presença dele — sem essa checagem um
    // POST forjado poderia injetar qualquer CSS (url(), por exemplo,
    // permite exfiltrar dados). Só passa se for exatamente uma dessas duas
    // declarações, com cor em #hex ou rgb(...).
    private static final Pattern ESTILO_SEGURO = Pattern.compile(
            "(color|background-color):\\s*(#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6}|rgb\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*\\))\\s*;?\\s*");

    // Pacote-privado (nao private) de proposito: permite teste unitario
    // direto (EditorSanitizarTest, mesmo pacote) sem reflection.
    static String sanitizar(String html) {
        if (html == null) {
            return null;
        }
        Document.OutputSettings semFormatacao = new Document.OutputSettings().prettyPrint(false);
        String limpo = Jsoup.clean(html, "", SAFELIST, semFormatacao);
        Document doc = Jsoup.parseBodyFragment(limpo);
        doc.outputSettings(semFormatacao);
        for (Element elemento : doc.select("[style]")) {
            if (!ESTILO_SEGURO.matcher(elemento.attr("style").trim()).matches()) {
                elemento.removeAttr("style");
            }
        }
        // rel/target de <a> não vêm do Jsoup (não estão na lista de
        // permissão) — setados aqui, sempre com o mesmo valor seguro, em
        // vez de confiar no que veio no POST.
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
        Object valorAtual = getValue();
        String valor = valorAtual != null ? valorAtual.toString() : "";

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", "box-editor", null);

        // O Quill toma conta desta div (toolbar + área editável); ele lê o
        // HTML já presente aqui como conteúdo inicial.
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-editor-quill", null);
        writer.write(valor);
        writer.endElement("div");

        // Campo real submetido com o form: o Quill em si não participa da
        // submissão nativa, o editor.js copia o HTML gerado pra cá a cada
        // mudança (evento text-change).
        writer.startElement("textarea", this);
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("class", "box-editor-valor", null);
        writer.write(valor);
        writer.endElement("textarea");

        writer.endElement("div");
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
