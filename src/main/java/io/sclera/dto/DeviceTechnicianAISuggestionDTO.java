package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class DeviceTechnicianAISuggestionDTO {
    private String id;
    private String deviceType;
    private String technicians;
    private String vdmsId;
}
