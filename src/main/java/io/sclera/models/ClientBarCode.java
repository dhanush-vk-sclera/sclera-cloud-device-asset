package io.sclera.models;

import io.sclera.dto.ClientBarCodeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "clientbarcodedetails",
        classes = {
                @ConstructorResult(
                        targetClass = ClientBarCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "ClientBarCode.getBarCodesByLocationIds",
        query = "SELECT cbc.id, cbc.device_id, cbc.location_id FROM client_bar_code cbc WHERE cbc.location_id IN ?1",
        resultSetMapping = "clientbarcodedetails"
)
@NamedNativeQuery(
        name = "ClientBarCode.getBarCodesByDeviceIds",
        query = "SELECT cbc.id, cbc.device_id, cbc.location_id FROM client_bar_code cbc WHERE cbc.device_id IN ?1",
        resultSetMapping = "clientbarcodedetails"
)

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientBarCode {

    @Id
    private String id;
    private String addedAt;
    private String addedBy;
    private String clientBarCodeId;
    private BigInteger updatedAt;
    private String updatedBy;
    private String vdmsId;
    private String batchId;

    @ManyToOne
    private Device device;

    @ManyToOne
    private Location location;

    private Boolean isDeleted;
}
