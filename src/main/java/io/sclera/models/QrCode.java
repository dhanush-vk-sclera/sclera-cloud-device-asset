package io.sclera.models;

import io.sclera.dto.QrCodeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;


@SqlResultSetMapping(
        name = "qrcodedetails",
        classes = {
                @ConstructorResult(
                        targetClass = QrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "updated_by", type = String.class),
                                @ColumnResult(name = "updated_time", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "QrCode.getQrCodesByDeviceIds",
        query = "(SELECT qc.id, qc.device_id, qc.location_id, qc.updated_by, qc.updated_time FROM qr_code qc WHERE qc.device_id IN (?1))\n" +
                "UNION ALL \n" +
                "(SELECT cqc.id, cqc.device_id, cqc.location_id, cqc.updated_by, cqc.updated_at as updated_time FROM client_qr_code cqc WHERE cqc.device_id IN (?1))\n",
        resultSetMapping = "qrcodedetails"
)


@NamedNativeQuery(
        name = "QrCode.getQrCodesByLocationIds",
        query = "(SELECT qc.id, qc.device_id, qc.location_id, qc.updated_by, qc.updated_time FROM qr_code qc WHERE qc.location_id IN ?1)\n" +
                "UNION ALL \n" +
                "(SELECT cqc.id, cqc.device_id, cqc.location_id, cqc.updated_by, cqc.updated_at as updated_time FROM client_qr_code cqc WHERE cqc.location_id IN ?1)\n",
        resultSetMapping = "qrcodedetails"
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByIds",
        query = "(SELECT qc.id, qc.device_id, qc.location_id, qc.updated_by, qc.updated_time FROM qr_code qc WHERE qc.id IN ?1)\n" ,
        resultSetMapping = "qrcodedetails"
)

@NamedNativeQuery(
        name = "QrCode.getClientQrCodeDetailsByIds",
        query = "(SELECT cqc.id, cqc.device_id, cqc.location_id, cqc.updated_by, cqc.updated_at as updated_time FROM client_qr_code cqc WHERE cqc.id IN ?1)" ,
        resultSetMapping = "qrcodedetails"
)

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrCode {
    @Id
    private String id;
    private String imageUrl;
    private String vdmsId;
    private String createdBy;
    private BigInteger creationTime;
    private String batchId;
    private String qrCodeLink;
    private String updatedTime;
    private String updatedBy;

    @ManyToOne
    private Device device;

    @ManyToOne
    private Location location;
    private Boolean isDeleted;

}