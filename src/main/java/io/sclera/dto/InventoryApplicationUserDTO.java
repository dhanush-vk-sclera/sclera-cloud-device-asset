package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryApplicationUserDTO {

    private String id;
    private String technicianId;
    private String email;
    private String type; // Field used in getAllApplicationUsersFromInventory for type
    private String userType; // Field used in getApplicationUsersFromInventory for type
    private String applicationId;
    private Integer status;
    private Integer syncStatus;
}
