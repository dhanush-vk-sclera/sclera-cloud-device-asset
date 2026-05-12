package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NfcDTO {

    private String id;
    private String createdBy;
    private BigInteger creationTime;
    private String deviceId;

    private String locationId;
    private String uuid;
    private String vdmsId;

    //nfcdetails
    public NfcDTO(String id, String deviceId, String locationId) {
        this.id = id;
        this.deviceId = deviceId;
        this.locationId = locationId;
    }
}
