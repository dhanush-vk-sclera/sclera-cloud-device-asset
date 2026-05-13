package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MyDevicesCompanyDTO;
import io.sclera.dto.MyDevicesSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: edge-only MyDevices service - replace with remote call */
@Service
public class MyDevicesService {

    public void startMyDevicesService() {}
    public void upsertMyDevicesCompany(String username, String vdmsid, MyDevicesCompanyDTO myDevicesCompany) {}
    public void updateMyDevicesEventData(JSONObject myDevicesEventData) {}
    public List<MyDevicesCompanyDTO> getMyDevicesCompanies(String vdmsId, Integer page, Integer size) { return Collections.emptyList(); }
    public List<MyDevicesSensorDTO> getMyDevicesSensors(String vdmsId, String companyId, Integer page, Integer size) { return Collections.emptyList(); }
    public void deleteMyDevicesCompany(String id) {}
    public void deleteMyDevicesSensor(String id) {}

    public String getDeviceIdByMyDevicesSensorId(String sensorId) {
        StubLog.warn("MyDevicesService", "getDeviceIdByMyDevicesSensorId", "AP-C3");
        return null;
    }
    public Integer getMyDevicesSensorCountByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorCountByDeviceId", "AP-C3");
        return 0;
    }
    public Boolean getMyDevicesSensorAlertStatusByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorAlertStatusByDeviceId", "AP-C3");
        return null;
    }
    public Set<MyDevicesSensorDTO> getDeviceMyDevicesSensors(String vdmsId, String companyId, String deviceId) {
        StubLog.warn("MyDevicesService", "getDeviceMyDevicesSensors", "AP-C3");
        return Collections.emptySet();
    }
    public List<SensorDTO> getMydevicesSensorsByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMydevicesSensorsByDeviceId", "AP-C3");
        return Collections.emptyList();
    }
    public Collection<? extends ConditionsDTO> listmydevicesDeviceAlertMessagesByDeviceIds(List<String> deviceIds) {
        StubLog.warn("MyDevicesService", "listmydevicesDeviceAlertMessagesByDeviceIds", "AP-C3");
        return Collections.emptyList();
    }
    public void updateMyDevicesSensorDeviceId(String oldDeviceId, String newDeviceId, Set<String> sensorIds) {
        StubLog.warn("MyDevicesService", "updateMyDevicesSensorDeviceId", "AP-C3");
    }
}
