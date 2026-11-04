package service;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.AssetMapper;
import model.Asset;
import repository.AssetRepository;
import repository.DailyQuoteRepository;
import repository.TransactionRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AssetService {

    @Inject
    AssetRepository assetRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    DailyQuoteRepository dailyQuoteRepository;

    @Inject
    AssetMapper assetMapper;

    public List<AssetDTO> getAllAssets() {
        List<Asset> assets = assetRepository.listAll();
        return assetMapper.listToDTOs(assets);
    }

    public AssetDTO getById(UUID id) {
        Asset asset = assetRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + id));
        return assetMapper.toDTO(asset);
    }

    @Transactional
    public AssetDTO create(AssetForm assetForm) {
        assetRepository.findByTicker(assetForm.ticker()).ifPresent(existing -> {
            throw new ConflictException("Asset with ticker '" + assetForm.ticker() + "' already exists.");
        });

        Asset asset = assetMapper.formToModel(assetForm);
        assetRepository.persist(asset);
        return assetMapper.toDTO(asset);
    }

    @Transactional
    public AssetDTO update(UUID id, AssetForm assetForm) {
        Asset asset = assetRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + id));

        if (assetForm.ticker() != null && !assetForm.ticker().isBlank()) {
            assetRepository.findByTicker(assetForm.ticker()).ifPresent(existing -> {
                if (!existing.id.equals(id)) {
                    throw new ConflictException("Asset with ticker '" + assetForm.ticker() + "' already exists for another asset.");
                }
            });
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
    public void delete(UUID id) {
        Asset asset = assetRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + id));

        if (!transactionRepository.listByAssetId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete asset with registered transactions.");
        }

        if (!dailyQuoteRepository.listByAssetId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete asset with registered daily quotes.");
        }

        assetRepository.delete(asset);
    }
}
