package br.com.feliva.bpm.avaliacao.model;

import br.com.feliva.sharedClass.db.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "area_conhecimento", schema = "avaliacao")
public class AreaConhecimento extends Model<UUID> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "area_conhecimento_id", updatable = false, nullable = false)
    private UUID areaConhecimentoId;

    @Column(nullable = false, unique = true, length = 30)
    private String cod;

    @Column(nullable = false, length = 250)
    private String descricao;

    public AreaConhecimento() {
    }

    public AreaConhecimento(UUID areaConhecimentoId, String cod, String descricao) {
        this.areaConhecimentoId = areaConhecimentoId;
        this.cod = cod;
        this.descricao = descricao;
    }

    @Override
    public UUID getMMId() {
        return this.areaConhecimentoId;
    }

    public UUID getAreaConhecimentoId() {
        return areaConhecimentoId;
    }

    public void setAreaConhecimentoId(UUID areaConhecimentoId) {
        this.areaConhecimentoId = areaConhecimentoId;
    }

    public UUID getId() {
        return areaConhecimentoId;
    }

    public void setId(UUID id) {
        this.areaConhecimentoId = id;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRotuloExibicao() {
        if (cod != null && !cod.isBlank()) {
            return cod + " - " + descricao;
        }
        return descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaConhecimento that = (AreaConhecimento) o;
        return Objects.equals(areaConhecimentoId, that.areaConhecimentoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaConhecimentoId);
    }

    @Override
    public String toString() {
        return getRotuloExibicao();
    }
}
