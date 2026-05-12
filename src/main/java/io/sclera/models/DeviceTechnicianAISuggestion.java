package io.sclera.models;

import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@SqlResultSetMapping(name = "deviceTechnicianAISuggestionMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceTechnicianAISuggestionDTO.class,
                        columns = {
                                @ColumnResult(name = "id",type = String.class),
                                @ColumnResult(name = "deviceType",type = String.class),
                                @ColumnResult(name = "technicians",type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class)

                        }
                )
        })
@NamedNativeQuery(
        name = "DeviceTechnicianAISuggestion.getAlldevicetechnician",
        query = "SELECT id, device_type AS deviceType, technicians, vdms_id AS vdmsId FROM device_technician_ai_suggestion",
        resultSetMapping = "deviceTechnicianAISuggestionMapping"
)
@NamedNativeQuery(
        name = "DeviceTechnicianAISuggestion.getdevicetechnicianbyid",
        query = "SELECT id, device_type AS deviceType, technicians, vdms_id AS vdmsId FROM device_technician_ai_suggestion WHERE id = ?1",
        resultSetMapping = "deviceTechnicianAISuggestionMapping"
)



@Entity
@Getter
@Setter
@Table(name = "device_technician_ai_suggestion")
public class DeviceTechnicianAISuggestion {
    @Id
    private String id;

    @Column(nullable = false)
    private String deviceType;

    @Column(columnDefinition = "json")
    private String technicians;

    @ManyToOne
    @JoinColumn(name = "vdms_id")
    private Vdms vdms;

}