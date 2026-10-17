package service;

import dto.asset.AssetDTO;
import jakarta.inject.Inject;
import mapper.AssetMapper;
import model.Asset;
import repository.AssetRepository;

import java.util.List;

public class AssetService {

    @Inject
    AssetRepository assetRepository;

    @Inject
    AssetMapper assetMapper;

    public List<AssetDTO> getAllAssets() {
        List<Asset> assets = assetRepository.listAll();
        return assetMapper.listToDTOs(assets);
    }
}
