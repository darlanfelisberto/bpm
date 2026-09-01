package br.edu.iffar.showcase.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servlet listener that tracks the number of currently active HTTP sessions.
 */
@WebListener
public class ActiveSessionListener implements HttpSessionListener {

    private static final AtomicInteger ACTIVE_SESSIONS = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ACTIVE_SESSIONS.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ACTIVE_SESSIONS.decrementAndGet();
    }

    public static int getActiveSessionsCount() {
        return Math.max(0, ACTIVE_SESSIONS.get());
    }
}
