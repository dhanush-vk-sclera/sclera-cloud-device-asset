package io.sclera.models;

import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@SqlResultSetMapping(
        name = "deviceonboardassigneemapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceOnboardStatusAssigneeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "device_onboard_status_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "DeviceOnboardStatusAssignee.getDeviceOnboardStatusAssignees",
        query = "SELECT dosa.id, dosa.type, dosa.email, dosa.device_onboard_status_id FROM device_onboard_status_assignee dosa"
                + " WHERE dosa.device_onboard_status_id = ?1 ",
        resultSetMapping = "deviceonboardassigneemapping"
)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeviceOnboardStatusAssignee {
    @Id
    private String id;
    private String type;
    private String email;

    @ManyToOne
    DeviceOnboardStatus device_onboard_status;
}
