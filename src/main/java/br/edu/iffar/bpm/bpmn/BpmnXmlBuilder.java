package br.edu.iffar.bpm.bpmn;

import br.edu.iffar.bpm.model.Atividade;
import br.edu.iffar.bpm.model.FluxoSequencia;
import br.edu.iffar.bpm.model.Processo;
import br.edu.iffar.bpm.model.Raia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Monta um XML BPMN 2.0 (incluindo DI para posicionamento) a partir dos dados
 * cadastrados no CRUD administrativo. Sem motor de execução: apenas
 * serialização + um layout automático simples (grade), suficiente para uma
 * visualização básica no bpmn-js.
 *
 * Layout: raias viram bandas horizontais empilhadas por ordem; atividades são
 * distribuídas da esquerda para a direita na ordem cadastrada (campo "ordem"),
 * centralizadas verticalmente na banda da sua raia.
 */
public class BpmnXmlBuilder {

    private static final String NS_BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String NS_BPMNDI = "http://www.omg.org/spec/BPMN/20100524/DI";
    private static final String NS_DI = "http://www.omg.org/spec/DD/20100524/DI";
    private static final String NS_DC = "http://www.omg.org/spec/DD/20100524/DC";

    private static final int LANE_HEIGHT = 150;
    private static final int LANE_X = 160;
    private static final int LANE_TOP_Y = 60;
    private static final int FIRST_ELEMENT_X = 260;
    private static final int ELEMENT_SPACING_X = 180;
    private static final int TASK_WIDTH = 100;
    private static final int TASK_HEIGHT = 80;
    private static final int EVENT_SIZE = 36;
    private static final int GATEWAY_SIZE = 50;

    public String gerar(Processo processo, List<Raia> raias, List<Atividade> atividades,
                         List<FluxoSequencia> fluxos) {

        String processId = "Process_" + processo.getId();
        Map<Long, Bounds> bounds = calcularLayout(raias, atividades);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<bpmn:definitions xmlns:bpmn=\"").append(NS_BPMN).append("\"")
                .append(" xmlns:bpmndi=\"").append(NS_BPMNDI).append("\"")
                .append(" xmlns:di=\"").append(NS_DI).append("\"")
                .append(" xmlns:dc=\"").append(NS_DC).append("\"")
                .append(" id=\"Definitions_").append(processo.getId()).append("\"")
                .append(" targetNamespace=\"https://iffar.edu.br/bpm\">\n");

        xml.append("  <bpmn:process id=\"").append(processId).append("\" name=\"")
                .append(esc(processo.getNome())).append("\" isExecutable=\"false\">\n");

        if (!raias.isEmpty()) {
            xml.append("    <bpmn:laneSet id=\"LaneSet_").append(processo.getId()).append("\">\n");
            for (Raia raia : raias) {
                xml.append("      <bpmn:lane id=\"Lane_").append(raia.getId())
                        .append("\" name=\"").append(esc(raia.getNome())).append("\">\n");
                for (Atividade a : atividades) {
                    if (a.getRaia() != null && a.getRaia().getId().equals(raia.getId())) {
                        xml.append("        <bpmn:flowNodeRef>Elemento_").append(a.getId())
                                .append("</bpmn:flowNodeRef>\n");
                    }
                }
                xml.append("      </bpmn:lane>\n");
            }
            xml.append("    </bpmn:laneSet>\n");
        }

        for (Atividade a : atividades) {
            String elId = "Elemento_" + a.getId();
            String localName = tagLocal(a.getTipoElemento().getTagBpmn());
            xml.append("    <bpmn:").append(localName).append(" id=\"").append(elId)
                    .append("\" name=\"").append(esc(a.getNome())).append("\">\n");
            for (FluxoSequencia f : fluxos) {
                if (f.getAtividadeDestino().getId().equals(a.getId())) {
                    xml.append("      <bpmn:incoming>Fluxo_").append(f.getId()).append("</bpmn:incoming>\n");
                }
            }
            for (FluxoSequencia f : fluxos) {
                if (f.getAtividadeOrigem().getId().equals(a.getId())) {
                    xml.append("      <bpmn:outgoing>Fluxo_").append(f.getId()).append("</bpmn:outgoing>\n");
                }
            }
            xml.append("    </bpmn:").append(localName).append(">\n");
        }

        for (FluxoSequencia f : fluxos) {
            xml.append("    <bpmn:sequenceFlow id=\"Fluxo_").append(f.getId())
                    .append("\" sourceRef=\"Elemento_").append(f.getAtividadeOrigem().getId())
                    .append("\" targetRef=\"Elemento_").append(f.getAtividadeDestino().getId()).append("\"");
            if (f.getRotuloCondicao() != null && !f.getRotuloCondicao().isBlank()) {
                xml.append(" name=\"").append(esc(f.getRotuloCondicao())).append("\"");
            }
            xml.append("/>\n");
        }

        xml.append("  </bpmn:process>\n");

        xml.append("  <bpmndi:BPMNDiagram id=\"Diagram_").append(processo.getId()).append("\">\n");
        xml.append("    <bpmndi:BPMNPlane id=\"Plane_").append(processo.getId())
                .append("\" bpmnElement=\"").append(processId).append("\">\n");

        int larguraDiagrama = larguraDiagrama(atividades);
        for (int i = 0; i < raias.size(); i++) {
            Raia raia = raias.get(i);
            xml.append("      <bpmndi:BPMNShape id=\"Lane_").append(raia.getId())
                    .append("_di\" bpmnElement=\"Lane_").append(raia.getId())
                    .append("\" isHorizontal=\"true\">\n");
            xml.append("        <dc:Bounds x=\"").append(LANE_X).append("\" y=\"")
                    .append(LANE_TOP_Y + i * LANE_HEIGHT).append("\" width=\"")
                    .append(larguraDiagrama).append("\" height=\"").append(LANE_HEIGHT).append("\"/>\n");
            xml.append("      </bpmndi:BPMNShape>\n");
        }

        for (Atividade a : atividades) {
            Bounds b = bounds.get(a.getId());
            xml.append("      <bpmndi:BPMNShape id=\"Elemento_").append(a.getId())
                    .append("_di\" bpmnElement=\"Elemento_").append(a.getId()).append("\">\n");
            xml.append("        <dc:Bounds x=\"").append(b.x).append("\" y=\"").append(b.y)
                    .append("\" width=\"").append(b.width).append("\" height=\"").append(b.height).append("\"/>\n");
            xml.append("      </bpmndi:BPMNShape>\n");
        }

        for (FluxoSequencia f : fluxos) {
            Bounds origem = bounds.get(f.getAtividadeOrigem().getId());
            Bounds destino = bounds.get(f.getAtividadeDestino().getId());
            xml.append("      <bpmndi:BPMNEdge id=\"Fluxo_").append(f.getId())
                    .append("_di\" bpmnElement=\"Fluxo_").append(f.getId()).append("\">\n");
            xml.append("        <di:waypoint x=\"").append(origem.x + origem.width).append("\" y=\"")
                    .append(origem.y + origem.height / 2).append("\"/>\n");
            xml.append("        <di:waypoint x=\"").append(destino.x).append("\" y=\"")
                    .append(destino.y + destino.height / 2).append("\"/>\n");
            xml.append("      </bpmndi:BPMNEdge>\n");
        }

        xml.append("    </bpmndi:BPMNPlane>\n");
        xml.append("  </bpmndi:BPMNDiagram>\n");
        xml.append("</bpmn:definitions>\n");

        return xml.toString();
    }

    private Map<Long, Bounds> calcularLayout(List<Raia> raias, List<Atividade> atividades) {
        Map<Long, Integer> laneIndexPorRaia = new LinkedHashMap<>();
        for (int i = 0; i < raias.size(); i++) {
            laneIndexPorRaia.put(raias.get(i).getId(), i);
        }

        Map<Long, Bounds> resultado = new LinkedHashMap<>();
        for (int i = 0; i < atividades.size(); i++) {
            Atividade a = atividades.get(i);
            String codigo = a.getTipoElemento().getCodigo();

            int largura;
            int altura;
            if ("START_EVENT".equals(codigo) || "END_EVENT".equals(codigo)) {
                largura = EVENT_SIZE;
                altura = EVENT_SIZE;
            } else if ("EXCLUSIVE_GATEWAY".equals(codigo) || "PARALLEL_GATEWAY".equals(codigo)) {
                largura = GATEWAY_SIZE;
                altura = GATEWAY_SIZE;
            } else {
                largura = TASK_WIDTH;
                altura = TASK_HEIGHT;
            }

            int laneIndex = 0;
            if (a.getRaia() != null && laneIndexPorRaia.containsKey(a.getRaia().getId())) {
                laneIndex = laneIndexPorRaia.get(a.getRaia().getId());
            }

            int x = FIRST_ELEMENT_X + i * ELEMENT_SPACING_X;
            int laneTopY = LANE_TOP_Y + laneIndex * LANE_HEIGHT;
            int y = laneTopY + (LANE_HEIGHT - altura) / 2;

            resultado.put(a.getId(), new Bounds(x, y, largura, altura));
        }
        return resultado;
    }

    private int larguraDiagrama(List<Atividade> atividades) {
        if (atividades.isEmpty()) {
            return 400;
        }
        return FIRST_ELEMENT_X + atividades.size() * ELEMENT_SPACING_X + 100;
    }

    private String tagLocal(String tagBpmn) {
        int idx = tagBpmn.indexOf(':');
        return idx >= 0 ? tagBpmn.substring(idx + 1) : tagBpmn;
    }

    private static String esc(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static final class Bounds {
        final int x;
        final int y;
        final int width;
        final int height;

        Bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
