package io.sclera.models;

import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;


@SqlResultSetMapping(
        name = "ManagedSoftwareMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ManagedSoftwareDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "applicationName", type = String.class),
                                @ColumnResult(name = "applicationType", type = String.class),
                                @ColumnResult(name = "url", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "subscriptionId", type = String.class),
                                @ColumnResult(name = "subscriptionType", type = String.class),
                                @ColumnResult(name = "unitPrice", type = Double.class),
                                @ColumnResult(name = "currency", type = String.class),
                                @ColumnResult(name = "subscriptionStartDate", type = BigInteger.class),
                                @ColumnResult(name = "subscriptionEndDate", type = BigInteger.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "applicationId", type = String.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "ManagedSoftware.getAllManagedSoftwares",
        query = "SELECT " +
                "ms.id, ms.name, ms.application_name as applicationName, ms.application_type as applicationType, ms.url, " +
                "ms.vendor, ms.subscription_id as subscriptionId, ms.subscription_type as subscriptionType, ms.unit_price as unitPrice, ms.currency, " +
                "ms.subscription_start_date as subscriptionStartDate, ms.subscription_end_date as subscriptionEndDate, ms.status,  ms.application_id as applicationId " +
                "FROM managed_software ms " +
                "WHERE ( " +
                "  (?1 = 'all') " +
                "  OR (?1 = 'active' AND ms.status = 'active') " +
                "  OR (?1 = 'expired' AND ms.status = 'expired') " +
                "  OR (?1 = 'others' AND ms.status NOT IN ('active','expired')) " +
                ") " +
                "AND (?2 = 'null' OR CONCAT_WS('', ms.name, ms.application_name, ms.vendor, ms.subscription_type) LIKE CONCAT('%', ?2, '%')) " +
                "LIMIT ?4 OFFSET ?3 ",
        resultSetMapping = "ManagedSoftwareMapping"
)

@NamedNativeQuery(
        name = "ManagedSoftware.getManagedSoftwareById",
        query = "SELECT " +
                "ms.id, ms.name, ms.application_name as applicationName, ms.application_type as applicationType, ms.url, " +
                "ms.vendor, ms.subscription_id as subscriptionId, ms.subscription_type as subscriptionType, ms.unit_price as unitPrice, ms.currency, " +
                "ms.subscription_start_date as subscriptionStartDate, ms.subscription_end_date as subscriptionEndDate, ms.status, ms.application_id as applicationId " +
                "FROM managed_software ms " +
                "WHERE ms.id = ?1",
        resultSetMapping = "ManagedSoftwareMapping"
)

@NamedNativeQuery(
        name = "ManagedSoftware.getManagedSoftwareByIdList",
        query = "SELECT " +
                "ms.id, ms.name, ms.application_name as applicationName, ms.application_type as applicationType, ms.url, " +
                "ms.vendor, ms.subscription_id as subscriptionId, ms.subscription_type as subscriptionType, ms.unit_price as unitPrice, ms.currency, " +
                "ms.subscription_start_date as subscriptionStartDate, ms.subscription_end_date as subscriptionEndDate, ms.status, ms.application_id as applicationId " +
                "FROM managed_software ms " +
                "WHERE ms.id IN ?1 " +
                "ORDER BY FIELD(ms.id, ?1)",
        resultSetMapping = "ManagedSoftwareMapping"
)

@SqlResultSetMapping(
        name = "ManagedSoftwareUsersMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ManagedSoftwareUsersDTO.class,
                        columns = {
                                @ColumnResult(name = "username", type = String.class),
                                @ColumnResult(name = "userUUID", type = String.class),
                                @ColumnResult(name = "accountType", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "riskStatus", type = Integer.class),
                                @ColumnResult(name = "deviceName", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "osType", type = String.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "ManagedSoftware.getManagedSoftwareUsers",
        query = "SELECT ds.username, ds.user_uuid as userUUID, ds.account_type as accountType, ds.email, dia.risk_status as riskStatus, ds.device_name as deviceName, ds.model, ds.os_type as osType " +
                "FROM managed_software ms " +
                "JOIN device_installed_apps dia ON ms.id = dia.managed_software_id " +
                "JOIN device_specification ds ON dia.device_specification_id = ds.id " +
                "WHERE ms.id = ?1",
        resultSetMapping = "ManagedSoftwareUsersMapping"
)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "managed_software")
public class ManagedSoftware {

    @Id
    private String id;

    private String name;

    private String applicationName;

    private String applicationType;

    private String url;

    private String vendor;

    private String subscriptionId;

    @Column(name = "subscription_type")
    private String subscriptionType;

    @Column(name = "unit_price")
    private double unitPrice;

    private String currency;

    @Column(name = "subscription_start_date")
    private BigInteger subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private BigInteger subscriptionEndDate;

    private String status;

    @Column(name = "application_id")
    private String applicationId;
}

