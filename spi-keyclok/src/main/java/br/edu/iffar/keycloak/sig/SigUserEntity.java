package br.edu.iffar.keycloak.sig;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_sig", schema = "sig")
public class SigUserEntity {
    @Id
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "auth_user_id")
    private UUID authUserId;

    protected SigUserEntity() {
    }
}
