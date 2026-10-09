package br.com.feliva.bpm.avaliacao.model;

import br.com.feliva.sharedClass.db.Model;
import jakarta.persistence.*;

@Entity
@Table(name = "tipo_status",schema = "avaliacao")
public class TipoStatus extends Model<String> {

    @Id
    @Column(name = "tipo_status_id", updatable = false, nullable = false)
    private String tipoStatusId;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Override
    public String getMMId() {
        return this.tipoStatusId;
    }

    public String getTipoStatusId() {
        return tipoStatusId;
    }

    public void setTipoStatusId(String tipoStatusId) {
        this.tipoStatusId = tipoStatusId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
