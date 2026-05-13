package io.sclera.models;

import io.sclera.dto.LorawanConfigurationDTO;
import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
//Shashikala
@SqlResultSetMapping(
        name = "allLorawanConfigurationMapping",
        classes = {
                @ConstructorResult(
                        targetClass = LorawanConfigurationDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "api_key", type = String.class),
                                @ColumnResult(name = "application_id", type = String.class),
                                @ColumnResult(name = "tenant_id", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "LorawanConfiguration.getLorawanAllConfigurations",
        query = "SELECT id, name, api_key, application_id, tenant_id, vdms_id FROM lorawan_configuration",
        resultSetMapping = "allLorawanConfigurationMapping"
)
public class LorawanConfiguration {

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String api_key;

    @Column
    private String application_id;

    @Column
    private String tenant_id;

//    @ManyToOne
//    @JoinColumn(name = "vdms_id", referencedColumnName = "id")
//    private Vdms vdms_id;

    @ManyToOne(optional = true)  // allow no Vdms reference
    @JoinColumn(name = "vdms_id", referencedColumnName = "id", nullable = true)
    private Vdms vdms_id;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lorawan_configuration")
    private Set<Lorawan_Sensor> lorawan_sensors;
}
