package io.sclera.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceNetworkSpecification {

    //mac-address is stored as ID
    @Id
    private String id;

    @Lob
    @Column(name = "network_interfaces", columnDefinition = "LONGTEXT")
    private String networkInterfaces;

    @Lob
    @Column(name = "network_settings", columnDefinition = "LONGTEXT")
    private String networkSettings;

    @Lob
    @Column(name = "network_ports", columnDefinition = "LONGTEXT")
    private String networkPorts;

    @Lob
    @Column(name = "network_processes", columnDefinition = "LONGTEXT")
    private String networkProcesses;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "device_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Device device;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
