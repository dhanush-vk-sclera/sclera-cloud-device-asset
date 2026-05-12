package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagedSoftwareDTO {
    private String id;
    private String name;
    private String applicationName;
    private String applicationType;
    private String url;
    private String vendor;
    private String subscriptionId;
    private String subscriptionType;
    private double unitPrice;
    private String currency;
    private BigInteger subscriptionStartDate;
    private BigInteger subscriptionEndDate;
    private String status;
    private String applicationId;

    public ManagedSoftwareDTO(String id, String name, String applicationName, String applicationType, String url, String vendor, String subscriptionId, String subscriptionType, double unitPrice, String currency, BigInteger subscriptionStartDate, BigInteger subscriptionEndDate, String status, String applicationId) {
        this.id = id;
        this.name = name;
        this.applicationName = applicationName;
        this.applicationType = applicationType;
        this.url = url;
        this.vendor = vendor;
        this.subscriptionId = subscriptionId;
        this.subscriptionType = subscriptionType;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.subscriptionStartDate = subscriptionStartDate;
        this.subscriptionEndDate = subscriptionEndDate;
        this.status = status;
        this.applicationId = applicationId;
    }
}
