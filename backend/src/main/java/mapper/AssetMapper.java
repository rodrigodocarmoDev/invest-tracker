package mapper;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import model.Asset;

import java.util.List;

public class AssetMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public AssetDTO toDTO(Asset asset) {
        return modelMapper.map(asset, AssetDTO.class);
    }

    public List<AssetDTO> listToDTOs (List<Asset> assets) {
        return assets.stream().map((Asset asset) -> toDTO(asset)).toList();
    }

    public Asset formToModel(AssetForm assetForm) {
        return modelMapper.map(assetForm, Asset.class);
    }

}
