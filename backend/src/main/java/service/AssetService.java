package service;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import jakarta.inject.Inject;
import mapper.AssetMapper;
import model.Asset;
import repository.AssetRepository;
import java.util.List;
import java.util.UUID;

public class AssetService {

    @Inject
    AssetRepository assetRepository;
    @Inject
    AssetMapper assetMapper;

    public List<AssetDTO> getAllAssets() {
        List<Asset> assets = assetRepository.listAll();
        return assetMapper.listToDTOs(assets);
    }

    public AssetDTO getById(UUID id) {
        Asset asset = assetRepository.findById(id);
        return assetMapper.toDTO(asset);
    }

    //@Transactional
    public AssetDTO create(AssetForm assetForm) {
        //Asset asset = new Asset();
        //asset.ticker = form.getTicker();
        //asset.type = form.getType();
        //asset.name = form.getName();

        Asset asset = assetRepository.create(assetForm);
        return assetMapper.toDTO(asset);
    }

    //@Transactional
    public AssetDTO update(UUID id, AssetForm assetForm) {
        Asset asset = assetRepository.findById(id);

        //asset.ticker = assetForm.getTicker();
        //asset.type = assetForm.getType();
        //asset.name = assetForm.getName();

        return assetMapper.toDTO(asset);
    }

    //@Transactional
    public void delete (UUID id) {
        assetRepository.deleteById(id);
    }


}
