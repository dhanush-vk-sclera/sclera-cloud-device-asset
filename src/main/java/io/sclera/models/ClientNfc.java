package io.sclera.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigInteger;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientNfc {

    @Id
    private String id;
    private String batchId;
    private String createdBy;
    private BigInteger creationTime;
    private String nfcId;
    private String uuid;
    private String vdmsId;

    @ManyToOne
    private Device device;

    private Boolean isDeleted;

    @ManyToOne
    private Location location;
}
