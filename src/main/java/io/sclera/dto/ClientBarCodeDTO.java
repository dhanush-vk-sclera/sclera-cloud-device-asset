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
public class ClientBarCodeDTO {

    private String id;
    private String addedAt;
    private String addedBy;
    private String clientBarCodeId;
    private String deviceId;
    private String locationId;
    private BigInteger updatedTime;
    private String updatedBy;
    private String vdmsId;
    private String batchId;

    //barcodedetails
    public ClientBarCodeDTO(String id, String deviceId, String locationId) {
        this.id = id;
        this.deviceId = deviceId;
        this.locationId = locationId;
    }
}
