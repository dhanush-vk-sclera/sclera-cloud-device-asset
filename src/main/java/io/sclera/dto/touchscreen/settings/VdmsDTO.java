package io.sclera.dto.touchscreen.settings;

import lombok.Data;

import java.math.BigInteger;

@Data
public class VdmsDTO {
    private String id;
    private String activation_status;
    private String location;
    private String timezone;
    private Boolean isConfigured;
    private String property_name;
    private String status;
    private String city;
    private String state;
    private String country;
    private String address;
    private Boolean is_block;
    private BigInteger creation_timestamp;
    private BigInteger last_seen;
    private BigInteger block_timestamp;
    private Integer zip;
    private String longitude;
    private String latitude;
    private String customer_org_id;
    private String publicKey;
    private String accessToken;
    private String refreshToken;
    private String password;
    private String image_url;
    private BigInteger activation_timestamp;
    private String deployment_type;
    private String region;
    private String adc_configuration_id;

    private Integer is_master;
    private Integer has_secondary_device;
    private String secondary_device_id;
    private String master_ip;
    private String slave_ip;

    public VdmsDTO(String id, String activation_status, String status, String location, String timezone, Boolean isConfigured) {
        this.id = id;
        this.activation_status = activation_status;
        this.status = status;
        this.location = location;
        this.timezone = timezone;
        this.isConfigured = isConfigured;
    }

    public VdmsDTO(String id, String customer_org_id, String adc_configuration_id, Integer zip) {
        this.id = id;
        this.customer_org_id = customer_org_id;
        this.adc_configuration_id = adc_configuration_id;
        this.zip = zip;
    }

    public Boolean getConfigured() {
        return isConfigured;
    }

    public void setConfigured(Boolean configured) {
        isConfigured = configured;
    }

    public VdmsDTO(String id, String activation_status, String property_name) {
        super();
        this.id = id;
        this.activation_status = activation_status;
        this.property_name = property_name;
    }

    public VdmsDTO(String id, String activation_status, Boolean isConfigured, String property_name) {
        this.id = id;
        this.activation_status = activation_status;
        this.isConfigured = isConfigured;
        this.property_name = property_name;
    }

    public VdmsDTO(String id, String activation_status, String status, String location, String timezone, String property_name, Boolean isConfigured , BigInteger activation_timestamp, String deployment_type) {
        this.id = id;
        this.activation_status = activation_status;
        this.status = status;
        this.location = location;
        this.timezone = timezone;
        this.property_name = property_name;
        this.isConfigured = isConfigured;
        this.activation_timestamp = activation_timestamp;
        this.deployment_type = deployment_type;
    }

    public VdmsDTO(String id, String property_name, String address, String city, String country, String state, Integer zip, String timezone, String image_url, String latitude,
                   String longitude , BigInteger activation_timestamp, String deployment_type, String region) {
        super();
        this.id = id;
        this.property_name = property_name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.timezone = timezone;
        this.image_url = image_url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.activation_timestamp = activation_timestamp;
        this.deployment_type = deployment_type;
        this.region = region;

    }

    // vdmsmasterslaveinfomapping
    public VdmsDTO(String id, Integer is_master, Integer has_secondary_device, String secondary_device_id, String master_ip, String slave_ip) {
        this.id = id;
        this.is_master = is_master;
        this.has_secondary_device = has_secondary_device;
        this.secondary_device_id = secondary_device_id;
        this.master_ip = master_ip;
        this.slave_ip = slave_ip;
    }

    // vdmsgetconfigurationinfomapping
    public VdmsDTO(String id, String activation_status, String status, String location, String timezone, String property_name, Boolean isConfigured, BigInteger activation_timestamp, String deployment_type, Integer is_master) {
        this.id = id;
        this.activation_status = activation_status;
        this.status = status;
        this.location = location;
        this.timezone = timezone;
        this.property_name = property_name;
        this.isConfigured = isConfigured;
        this.activation_timestamp = activation_timestamp;
        this.deployment_type = deployment_type;
        this.is_master = is_master;
    }

    public VdmsDTO() {
        super();
        // TODO Auto-generated constructor stub
    }
}
