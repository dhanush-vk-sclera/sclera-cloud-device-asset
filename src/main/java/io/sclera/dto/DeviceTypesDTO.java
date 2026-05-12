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
public class DeviceTypesDTO {

    private String id;
    private String name;
    private BigInteger creationTimestamp;
    private BigInteger updatedTimestamp;
    private String oldName;

    public DeviceTypesDTO(String id, String name, BigInteger updatedTimestamp) {
        this.id = id;
        this.name = name;
        this.updatedTimestamp = updatedTimestamp;
    }

    public DeviceTypesDTO(String name, String oldName) {
        this.name = name;
        this.oldName = oldName;
    }
}
