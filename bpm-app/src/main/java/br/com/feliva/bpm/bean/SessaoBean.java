package br.com.feliva.bpm.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class SessaoBean {

    private static final Logger LOGGER = Logger.getLogger(SessaoBean.class.getName());

    public void sair() {
        String provedor = System.getenv("OIDC_PROVIDER_BASE_URL");
        String cliente = System.getenv("OIDC_CLIENT_ID");
        if (provedor == null || provedor.isBlank() || cliente == null || cliente.isBlank()) {
            throw new FacesException("Configuração OIDC necessária para sair não está disponível.");
        }

        FacesContext faces = FacesContext.getCurrentInstance();
        ExternalContext contexto = faces.getExternalContext();
        HttpServletRequest requisicao = (HttpServletRequest) contexto.getRequest();
        String logout = provedor.replaceAll("/+$", "")
                + "/protocol/openid-connect/logout?client_id="
                + URLEncoder.encode(cliente, StandardCharsets.UTF_8);

        try {
            requisicao.logout();
            contexto.invalidateSession();
            contexto.redirect(logout);
            faces.responseComplete();
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Falha ao encerrar a sessão do BPM.", e);
            throw new FacesException("Não foi possível encerrar a sessão.", e);
        }
    }
}
