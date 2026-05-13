package io.sclera.service;

import org.springframework.stereotype.Service;
import java.util.List;

/** STUB: replace with remote call to AP-C6 */
@Service
public class UserActionLogService {
    public void addUserAction(String username, String asset, String update, String s, String success, String assetInfo, String id) {}
    public void batchUpdateUserActionLogs(List<UserActionLogDTO> logs) {}
}
