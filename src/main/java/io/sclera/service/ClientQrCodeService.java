package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class ClientQrCodeService {
    public JSONArray getDeviceIdsTaggedToClientQrCode(String id) { return new JSONArray(); }
    public Integer getClientQrCodeCountByDeviceId(String deviceId) { return 0; }
    public BigInteger maxUpdatedClientQrCodeTimeStamp(String deviceId) { return null; }

    public JSONArray getLocationIdsTaggedToClientQrCode(String id) {
        StubLog.warn("ClientQrCodeService", "getLocationIdsTaggedToClientQrCode", "AP-C1edge");
        return new JSONArray();
    }
}
