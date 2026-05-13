package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.QrCodeDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class QrCodeService {
    public JSONArray getDeviceIdsTaggedToQrCode(String qrCodeId) { return new JSONArray(); }
    public Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceIds) { return Collections.emptySet(); }
    public Integer getQrCodeCountByDeviceId(String deviceId) { return 0; }
    public BigInteger getMaxUpdatedQrCodeTimeStamp(String deviceId) { return null; }

    public JSONArray getLocationIdsTaggedToQrCode(String qrCodeId) {
        StubLog.warn("QrCodeService", "getLocationIdsTaggedToQrCode", "AP-C1edge");
        return new JSONArray();
    }
    public Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds) {
        StubLog.warn("QrCodeService", "getQrCodesByLocationIds", "AP-C1edge");
        return Collections.emptySet();
    }
}
