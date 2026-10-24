package service;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import exception.custom.ConflictException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

    @Transactional
    public AssetDTO create(AssetForm assetForm) {
        Asset asset = assetMapper.formToModel(assetForm);
        assetRepository.persist(asset);
        return assetMapper.toDTO(asset);
    }

    @Transactional
    public AssetDTO update(UUID id, AssetForm assetForm) {

        Asset asset = assetRepository.findByIdOptional(id).orElseThrow();

        if (assetForm.ticker() != null && !assetForm.ticker().isBlank()) {
        asset.ticker = assetForm.ticker();
        }

        if (assetForm.type() != null && !assetForm.type().isBlank()) {
            asset.type = assetForm.type();
        }

        if (assetForm.name() != null && !assetForm.name().isBlank()) {
            asset.name = assetForm.name();
        }
        return assetMapper.toDTO(asset);
    }

    @Transactional
    public void delete (UUID id) {
        Asset asset = assetRepository.findByIdOptional(id).orElseThrow();
        if (!transactionRepository.listByAssetId(id).isEmpty()) {
            throw new ConflictException("Not possible to exclude asset with registered transactions.");
        }

        if (!dailyQuoteRepository.listByAssetId(id).isEmpty()) {
            throw new ConflictException("Not possible to exclude asset with registered daily quotes.");
        }
        assetRepository.delete(asset);
    }

}
