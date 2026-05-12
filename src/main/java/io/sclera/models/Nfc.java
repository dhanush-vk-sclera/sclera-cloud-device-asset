package io.sclera.models;

import io.sclera.dto.NfcDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;


@SqlResultSetMapping(
        name = "nfcdetails",
        classes = {
                @ConstructorResult(
                        targetClass = NfcDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "Nfc.getNfcsByDeviceIds",
        query = "(SELECT nfc.id, nfc.device_id, nfc.location_id FROM nfc WHERE nfc.device_id IN (?1))\n" +
                "UNION ALL \n" +
                "(SELECT cnfc.id, cnfc.device_id, cnfc.location_id FROM client_nfc cnfc WHERE cnfc.device_id IN (?1))\n",
        resultSetMapping = "nfcdetails"
)


@NamedNativeQuery(
        name = "Nfc.getNfcsByLocationIds",
        query = "(SELECT nfc.id, nfc.device_id, nfc.location_id FROM nfc WHERE nfc.location_id IN ?1)\n" +
                "UNION ALL \n" +
                "(SELECT cnfc.id, cnfc.device_id, cnfc.location_id FROM client_nfc cnfc WHERE cnfc.location_id IN ?1)\n",
        resultSetMapping = "nfcdetails"
)

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Nfc {
    @Id
    private String id;
    private String createdBy;
    private BigInteger creationTime;
    private String uuid;
    private String vdmsId;

    @ManyToOne
    private Device device;

    @ManyToOne
    private Location location;
    private Boolean isDeleted;
}
