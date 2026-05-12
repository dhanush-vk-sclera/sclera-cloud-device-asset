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
@NoArgsConstructor
@AllArgsConstructor
public class ClientQrCode {
    @Id
    private String id;
    private String addedAt;
    private String addedBy;
    private String clientQrCodeId;
    private BigInteger updatedAt;
    private String updatedBy;
    private String vdmsId;
    private String batchId;

    @ManyToOne
    private Device device;

    @ManyToOne
    private Location location;

    private Boolean isDeleted;
}
