package io.sclera.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ManagedSoftwareUsersDTO {
    private String username;
    private String userUUID;
    private String accountType;
    private String email;
    private Integer riskStatus;
    private String deviceName;
    private String model;
    private String osType;
}
