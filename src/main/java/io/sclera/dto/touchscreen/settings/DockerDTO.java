package io.sclera.dto.touchscreen.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DockerDTO {
    private String name;
    private String vdms_id;
    private Boolean host;
    private String gateway;
    private String external_ip_address;
    private String system_type;
    private String internal_ip_address;
    private String public_ip_address;
    private Boolean internet_required;
    private String primary_dns;
    private String secondary_dns;
    private Boolean is_static;
    private Boolean is_tagged;
    private Integer vlan_id;
    private String interface_out;
    private String macvlan_name;
    private Integer cidr;
    private String configuration_status;
    private String approval_status;
    private String email;
    private String vendor_org_id;
    private String internet_status;
    private List<ConcurrentHashMap<String, String>> extraHost;
    private Integer network_origin;


    public DockerDTO(String name, String vdms_id, Boolean host, String gateway, String external_ip_address, String system_type, String internal_ip_address, String public_ip_address, Boolean internet_required, String primary_dns, String secondary_dns, Boolean is_static, Boolean is_tagged, Integer vlan_id, String interface_out, String macvlan_name, Integer cidr,String configuration_status,String approval_status,String email, String vendor_org_id, Integer network_origin) {
        this.name = name;
        this.vdms_id = vdms_id;
        this.host = host;
        this.gateway = gateway;
        this.external_ip_address = external_ip_address;
        this.system_type = system_type;
        this.internal_ip_address = internal_ip_address;
        this.public_ip_address = public_ip_address;
        this.internet_required = internet_required;
        this.primary_dns = primary_dns;
        this.secondary_dns = secondary_dns;
        this.is_static = is_static;
        this.is_tagged = is_tagged;
        this.vlan_id = vlan_id;
        this.interface_out = interface_out;
        this.macvlan_name = macvlan_name;
        this.cidr = cidr;
        this.configuration_status = configuration_status;
        this.approval_status = approval_status;
        this.email = email;
        this.vendor_org_id = vendor_org_id;
        this.network_origin = network_origin;
    }
    
    
//    public DockerDTO(String name, String vdms_id, Boolean host, String gateway, String external_ip_address,
//			String system_type, String internal_ip_address, String public_ip_address, Boolean internet_required,
//			String primary_dns, String secondary_dns, Boolean is_static, Boolean is_tagged, Integer vlan_id,
//			String interface_out, String macvlan_name, Integer cidr, String configuration_status,
//			String approval_status, String vendor_org_id, String internet_status) {
//		super();
//		this.name = name;
//		this.vdms_id = vdms_id;
//		this.host = host;
//		this.gateway = gateway;
//		this.external_ip_address = external_ip_address;
//		this.system_type = system_type;
//		this.internal_ip_address = internal_ip_address;
//		this.public_ip_address = public_ip_address;
//		this.internet_required = internet_required;
//		this.primary_dns = primary_dns;
//		this.secondary_dns = secondary_dns;
//		this.is_static = is_static;
//		this.is_tagged = is_tagged;
//		this.vlan_id = vlan_id;
//		this.interface_out = interface_out;
//		this.macvlan_name = macvlan_name;
//		this.cidr = cidr;
//		this.configuration_status = configuration_status;
//		this.approval_status = approval_status;
//		this.vendor_org_id = vendor_org_id;
//		this.internet_status = internet_status;
//	}

    public DockerDTO(String name, String vdms_id, Boolean host, String gateway, String external_ip_address, String system_type, String internal_ip_address, String public_ip_address, Boolean internet_required, String primary_dns, String secondary_dns, Boolean is_static, Boolean is_tagged, Integer vlan_id, String interface_out, String macvlan_name, Integer cidr,String configuration_status,String approval_status,String email, String vendor_org_id, String internet_status, Integer network_origin) {
        this.name = name;
        this.vdms_id = vdms_id;
        this.host = host;
        this.gateway = gateway;
        this.external_ip_address = external_ip_address;
        this.system_type = system_type;
        this.internal_ip_address = internal_ip_address;
        this.public_ip_address = public_ip_address;
        this.internet_required = internet_required;
        this.primary_dns = primary_dns;
        this.secondary_dns = secondary_dns;
        this.is_static = is_static;
        this.is_tagged = is_tagged;
        this.vlan_id = vlan_id;
        this.interface_out = interface_out;
        this.macvlan_name = macvlan_name;
        this.cidr = cidr;
        this.configuration_status = configuration_status;
        this.approval_status = approval_status;
        this.email = email;
        this.vendor_org_id = vendor_org_id;
        this.internet_status = internet_status;
        this.network_origin = network_origin;
    }

    // checkifhostpresentbynetworkoriginmapping
    public DockerDTO(String name, Boolean host, Integer network_origin) {
        this.name = name;
        this.host = host;
        this.network_origin = network_origin;
    }

    // checkifhostnetworkspresentmapping
    public DockerDTO(Integer network_origin, Boolean host) {
        this.network_origin = network_origin;
        this.host = host;
    }

    // dockernameslistmapping
    public DockerDTO(String name, Integer network_origin) {
        this.name = name;
        this.network_origin = network_origin;
    }
}
