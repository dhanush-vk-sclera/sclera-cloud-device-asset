package io.sclera.models;


import javax.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceInstalledApps {

    @Id
    private String id;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "version", length = 255)
    private String version;

    @Column(name = "device_id", nullable = true)
    private String deviceId;

    @Column(name = "device_specification_id", nullable = true)
    private String deviceSpecificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "device_specification_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DeviceSpecification deviceSpecification;

    @Column(name = "managed_software_id", nullable = true)
    private String managedSoftwareId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "managed_software_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ManagedSoftware managedSoftware;

    @Column(name = "risk_status")
    private Integer riskStatus;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}