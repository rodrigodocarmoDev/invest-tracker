package mapper;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import dto.wallet.WalletDTO;
import dto.wallet.WalletForm;
import model.Asset;
import model.Wallet;

import java.util.List;

public class WalletMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public WalletDTO toDTO(Wallet wallet) {
        return modelMapper.map(wallet, WalletDTO.class);
    }

    public List<WalletDTO> listToDTOs (List<Wallet> wallets) {
        return wallets.stream().map((Wallet wallet) -> toDTO(wallet)).toList();
    }

    public Wallet formToModel(WalletForm walletForm) {
        return modelMapper.map(walletForm, Wallet.class);
    }
}
