package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientNfcDTO {

    private String id;
    private String batchId;
    private String createdBy;
    private BigInteger creationTime;

    private String deviceId;
    private String locationId;
    @JsonAlias("nfc_id")
    private String nfcId;
    private String uuid;

    private String vdmsId;

    private String updatedBy;
}
