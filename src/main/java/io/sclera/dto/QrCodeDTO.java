package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrCodeDTO {

    private String id;
    private String imageUrl;
    private String locationId;
    private String vdmsId;
    private String deviceId;
    private String createdBy;
    private BigInteger creationTime;
    private String batchId;
    private String qrCodeLink;
    private String updatedTime;
    private String updatedBy;

    private Boolean isDeleted;

    //qrcodedetails
    public QrCodeDTO(String id, String deviceId, String locationId, String updatedBy, String updatedTime) {
        this.id = id;
        this.deviceId = deviceId;
        this.locationId = locationId;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
    }


}


