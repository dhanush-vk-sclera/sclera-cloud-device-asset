package io.sclera.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "application_user")
public class ApplicationUser {

    @Id
    private String id;

    @Column(name = "technician_id")
    private String technicianId;

    private String email;

    private String type;

    @Column(name = "managed_software")
    private String managedSoftwareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_software", referencedColumnName = "id", insertable = false, updatable = false)
    private ManagedSoftware managedSoftware;
}
