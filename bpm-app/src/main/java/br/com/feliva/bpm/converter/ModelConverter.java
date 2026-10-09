package br.com.feliva.bpm.converter;

import br.com.feliva.sharedClass.db.Model;
import br.edu.iffar.box.converter.EntityConverter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Named;

@Named("modelConverter")
@ApplicationScoped
@FacesConverter(value = "modelConverter", forClass = Model.class, managed = true)
public class ModelConverter extends EntityConverter {
}
