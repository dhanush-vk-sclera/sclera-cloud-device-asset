package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.BuildingDTO;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.dto.DeviceTypesDTO;
import io.sclera.dto.ProductDTO;
import io.sclera.dto.touchscreen.SnmpValuesDTO;
import io.sclera.dto.touchscreen.settings.UserDTO;
import io.sclera.utils.StubLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class APICallService {

    private static final Logger log = LoggerFactory.getLogger(APICallService.class);

    public List<UserDTO> getUsersByOrgId(String organisation_id, String vdms_id) { return Collections.emptyList(); }
    public List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id) { return Collections.emptyList(); }

    public Flux<org.json.JSONObject> sendDescription(org.json.JSONObject requestBody, String vdmsId, String technicianId,
                                                     String technicianName, String contactNumber,
                                                     String formattedDateTime, String aiCallLogId) {
        return Flux.empty();
    }

    public ResponseEntity<String> sendCallFlowMail(JSONObject payload) { return ResponseEntity.ok(""); }
    public ResponseEntity<String> sendCallFlowMessage(JSONObject payload) { return ResponseEntity.ok(""); }

    public String getDeviceHostNameByIP(String a, String b) { return null; }
    public ProductDTO getProductDetailsByModelAndMBV(String model, String mbv) { return null; }
    public ProductDTO getProductDetailsByProductId(String productId) { return null; }

    public JSONArray getNfcIdsByVdmsAndType(String vdmsId, String type) { return new JSONArray(); }
    public JSONArray getQrCodeIdsByVdmsIdAndType(String vdmsId, String type) { return new JSONArray(); }
    public void deleteDigitalTwinImageUrl(Set<String> imageUrls, String username, String vdmsId) {}

    public JSONArray getTemporaryProductByIds(Set<String> ids) { return new JSONArray(); }
    public void deleteTemporaryProductByIds(Set<String> ids) {}

    public Boolean syncBuildingToADC(Object dto, String orgId, String configId) { return Boolean.FALSE; }
    public BuildingDTO addSingleBuildingObject(String locationId, String vdmsId) { return null; }
    public Boolean deleteBuildingFromADC(String orgId, String configId, List<String> propertyIds) { return Boolean.FALSE; }
    public List<BuildingDTO> getAllLocations(String vdmsId) { return Collections.emptyList(); }
    public String getFloorPathByFloorId(Object a, String vdmsId, String buildingId, String floorId) { return null; }

    public void generateChatbotMessage(String query, Object emitter) {}
    public void updateChatbotDeviceData(JSONArray bodyArray) {}

    public Set<ClientBarCodeDTO> getAllClientBarCodeByVdmsId(String vdmsId, int page, int size) { return Collections.emptySet(); }
    public Set<ClientBarCodeDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int page, int size) { return Collections.emptySet(); }

    public void sendAgentDataToInventory(Object obj) {}
    public Set<DeviceTypesDTO> getUpdatedAssetTypes(Object ts, int page, int size, String search, String vdmsId) { return Collections.emptySet(); }

    public Object updatePropertyDetails(String vdmsId) { return null; }
    public void updateVdmsDetailCloud(String vdmsId, Object dto) {}
    public Object getVendorByMacAddress(String mac) { return null; }
    public void syncAllAttribute(String ip) {}
    public void syncBacnet(String ip) {}
    public void syncSnmpWalk(String ip) {}
    public void snmpInterface(String ip) {}
    public void snmpTopology(String ip) {}
    public void internetConnectivity(String ip) {}
    public List<Object> getAllVendorsByOrganisationId(String orgId, String vdmsId, String dockerName) { return Collections.emptyList(); }
    public Object getTransferVendor(String vdmsId, String dockerName) { return null; }
    public String getCustomerOrgIdByVdmsId(String vdmsId) { return null; }
    public void updateVdmsTranfer(String vdmsId) {}
    public Object updateVdmsStatus(String vdmsId) { return null; }
    public void updateQrCodeSyncByVdmsId(String vdmsId) {}
    public void updateNfcSyncByVdmsId(String vdmsId) {}
    public JSONArray getJSONArrayFromJSONString(String json, Class<String> clazz) { return new JSONArray(); }
    public void syncSnmpInterfacebyDeviceId(String deviceId, SnmpValuesDTO dto) {}

    public Object fetchMeasuringInstruments() {
        StubLog.warn("APICallService", "fetchMeasuringInstruments", "AP-C1edge");
        return null;
    }
    public void syncLocationToADC(java.util.List<io.sclera.dto.LocationDTO> locations,
                                   String orgId, String configId, String vdmsId) {
        StubLog.warn("APICallService", "syncLocationToADC", "AP-C1edge");
    }
    public void deleteLocationFromADC(String orgId, String configId, String vdmsId,
                                       String buildingId, java.util.List<String> locationIds) {
        StubLog.warn("APICallService", "deleteLocationFromADC", "AP-C1edge");
    }
}
