package io.sclera.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceSpecificationDTO {
    private String userName;
    private String accountType;
    private String emailId;
    private String deviceId;
    private String deviceName;
    private String model;
    private JSONObject cpuInfo;
    private JSONArray ramInfo;
    private JSONObject osInfo;
    private JSONObject locationInfo;
    private JSONObject batteryInfo;
    private JSONArray videoCards;
    private JSONArray soundDevices;
    private JSONObject bios;
    private JSONArray diskDrives;
    private JSONArray networkPorts;
    private JSONArray networkSettings;
    private JSONArray networkInterfaces;
    private JSONArray processes;
    private JSONArray networkProcesses;
    private JSONObject childDevices;
}