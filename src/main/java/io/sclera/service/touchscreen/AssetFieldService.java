package io.sclera.service.touchscreen;

import io.sclera.Repository.AssetFieldRepository;
import io.sclera.dto.AssetFieldDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@Slf4j
public class AssetFieldService {

    private final AssetFieldRepository assetFieldRepository;

    public AssetFieldService(AssetFieldRepository assetFieldRepository) {
        this.assetFieldRepository = assetFieldRepository;
    }

    public List<AssetFieldDTO> getAssetFields(HttpServletRequest httpServletRequest) {
        log.info("Fetching asset fields for touchscreen");
        return assetFieldRepository.getAllAssetFields();
    }
}
