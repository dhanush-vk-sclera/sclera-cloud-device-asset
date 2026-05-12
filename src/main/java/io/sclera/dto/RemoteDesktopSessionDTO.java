package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemoteDesktopSessionDTO {

    private String sessionId;
    private String initiatedBy;
    private String url;
    private Boolean isAuthEnabled;
    private Integer acknowledged;
}
