package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.NfcDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class NfcService {
    public JSONArray getDeviceIdsTaggedToNfc(String nfcId) { return new JSONArray(); }
    public Set<NfcDTO> getNfcsByDeviceIds(Set<String> deviceIds) { return Collections.emptySet(); }
    public Integer getQrNfcCountByDeviceId(String deviceId) { return 0; }

    public JSONArray getLocationIdsTaggedToNfc(String nfcId) {
        StubLog.warn("NfcService", "getLocationIdsTaggedToNfc", "AP-C1edge");
        return new JSONArray();
    }
    public Set<NfcDTO> getNfcsByLocationIds(Set<String> locationIds) {
        StubLog.warn("NfcService", "getNfcsByLocationIds", "AP-C1edge");
        return Collections.emptySet();
    }
}
