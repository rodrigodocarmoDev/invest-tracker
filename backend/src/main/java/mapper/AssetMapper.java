package mapper;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import jakarta.enterprise.context.ApplicationScoped;
import model.Asset;

import java.util.List;

@ApplicationScoped
public class AssetMapper {

    public AssetDTO toDTO(Asset asset) {
        if (asset == null) return null;
        return new AssetDTO(asset.id, asset.ticker, asset.type, asset.name);
    }

    public List<AssetDTO> listToDTOs(List<Asset> assets) {
        if (assets == null) return List.of();
        return assets.stream().map(this::toDTO).toList();
    }

    public Asset formToModel(AssetForm assetForm) {
        if (assetForm == null) return null;
        Asset asset = new Asset();
        asset.ticker = assetForm.ticker();
        asset.type = assetForm.type();
        asset.name = assetForm.name();
        return asset;
    }
}
