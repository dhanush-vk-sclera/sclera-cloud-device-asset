package io.sclera.models;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSpecification {

    //mac-address is stored as ID
    @Id
    private String id;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "account_type", length = 255)
    private String accountType;

    @Column(name = "user_uuid", length = 255)
    private String userUUID;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "os_type", length = 255)
    private String osType;

    @Column(name = "location_info", length = 1024)
    private String locationInfo;

    @Column(name = "os_info", length = 1024)
    private String osInfo;

    @Column(name = "cpu_info", length = 512)
    private String cpuInfo;

    @Lob
    @Column(name = "disk_drives", columnDefinition = "LONGTEXT")
    private String diskDrives;

    @Lob
    @Column(name = "physical_disks", columnDefinition = "LONGTEXT")
    private String physicalDisks;

    @Column(name = "bios", length = 512)
    private String bios;

    @Column(name = "ram_info", length = 512)
    private String ramInfo;

    @Column(name = "video_cards", length = 1024)
    private String videoCards;

    @Column(name = "sound_devices", length = 1024)
    private String soundDevices;

    @Column(name = "battery_info", length = 255)
    private String batteryInfo;

    @Lob
    @Column(name = "processes", columnDefinition = "LONGTEXT")
    private String processes;

    @Lob
    @Column(name = "system_updates", columnDefinition = "LONGTEXT")
    private String systemUpdates;

    // Relationship mapping (lazy fetch, optional)
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "device_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Device device;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deviceSpecification")
    private Set<DeviceInstalledApps> deviceInstalledApps;

    @Lob
    @Column(name = "child_devices", columnDefinition = "LONGTEXT")
    private String childDevices;

    @Override
    public int hashCode() {
        return Objects.hash(id); // Use only basic fields like id
    }

}
