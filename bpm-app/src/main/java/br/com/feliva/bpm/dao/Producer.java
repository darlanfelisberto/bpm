package br.com.feliva.bpm.dao;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.sql.DataSource;

@ApplicationScoped
public class Producer {

    @Resource(lookup = "jdbc/bpmDS")
    private DataSource dataSource;

    @Produces
    @Default
    public DataSource produceDataSource() {
        return this.dataSource;
    }
}
