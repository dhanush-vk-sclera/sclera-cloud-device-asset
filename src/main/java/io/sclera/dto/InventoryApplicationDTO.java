package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryApplicationDTO {

    private String id;
    private String applicationType;
    private String applicationName;
    private String number;
    private String url;
    private String vendor;
    private String subscriptionId;
    private String subscriptionType;
    private Double unitPrice;
    private String currency;
    private Long subscriptionStartDate;
    private Long subscriptionEndDate;
    private String status;
    private Integer syncStatus;
    private Double total_license;
    private Double license_used;
}
