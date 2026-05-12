package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientQrCodeDTO {
    @Id
    private String id;
    private String addedAt;
    private String addedBy;
    private String clientQrCodeId;
    private String deviceId;
    private String locationId;
    private BigDecimal updatedTime;
    private String updatedBy;
    private String vdmsId;
    private String batchId;
}
