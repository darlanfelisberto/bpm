package br.edu.iffar.bpm.rest;

import br.edu.iffar.bpm.model.Processo;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Ponte entre o bpmn-js Modeler (no navegador) e o processo persistido:
 * GET devolve o XML atual (ou um esqueleto vazio se o processo ainda não
 * foi editado); POST substitui o XML pelo que o usuário acabou de salvar
 * no diagrama.
 */
@Path("/processos/{id}/diagrama")
public class ProcessoDiagramaResource {

    @Inject
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Transactional
    public Response obter(@PathParam("id") Long id) {
        Processo processo = em.find(Processo.class, id);
        if (processo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String xml = processo.getXmlBpmn() != null ? processo.getXmlBpmn() : xmlVazio(processo);
        return Response.ok(xml).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response salvar(@PathParam("id") Long id, String xml) {
        Processo processo = em.find(Processo.class, id);
        if (processo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        processo.setXmlBpmn(xml);
        return Response.noContent().build();
    }

    private String xmlVazio(Processo processo) {
        String pid = String.valueOf(processo.getId());
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
                + " xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\""
                + " xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\""
                + " xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\""
                + " id=\"Definitions_" + pid + "\" targetNamespace=\"https://iffar.edu.br/bpm\">\n"
                + "  <bpmn:process id=\"Process_" + pid + "\" name=\"" + esc(processo.getNome())
                + "\" isExecutable=\"false\"/>\n"
                + "  <bpmndi:BPMNDiagram id=\"Diagram_" + pid + "\">\n"
                + "    <bpmndi:BPMNPlane id=\"Plane_" + pid + "\" bpmnElement=\"Process_" + pid + "\"/>\n"
                + "  </bpmndi:BPMNDiagram>\n"
                + "</bpmn:definitions>\n";
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
}
