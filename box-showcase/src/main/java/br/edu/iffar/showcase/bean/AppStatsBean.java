package br.edu.iffar.showcase.bean;

import br.edu.iffar.showcase.listener.ActiveSessionListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * Backing bean providing real-time application statistics such as active sessions count.
 */
@Named("appStatsBean")
@ApplicationScoped
public class AppStatsBean {

    public int getActiveSessions() {
        return ActiveSessionListener.getActiveSessionsCount();
    }
}
