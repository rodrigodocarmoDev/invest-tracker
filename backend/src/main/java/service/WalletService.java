package service;

import dto.wallet.WalletDTO;
import dto.wallet.WalletForm;
import jakarta.inject.Inject;
import mapper.WalletMapper;
import model.Wallet;
import repository.WalletRepository;
import java.util.List;
import java.util.UUID;

public class WalletService {

    @Inject
    WalletRepository walletRepository;
    @Inject
    WalletMapper walletMapper;

    public List<WalletDTO> getAllWallets() {
        List<Wallet> wallets = walletRepository.listAll();
        return walletMapper.listToDTOs(wallets);
    }

    public WalletDTO getById(UUID id) {
        Wallet wallet = walletRepository.findById(id);
        return walletMapper.toDTO(wallet);
    }

    //@Transactional
    public WalletDTO create(WalletForm walletForm) {
        Wallet wallet = walletRepository.create(walletForm);
        return walletMapper.toDTO(wallet);
    }

    //@Transactional
    public WalletDTO update(UUID id, WalletForm walletForm) {
        Wallet wallet = walletRepository.findById(id);
        return walletMapper.toDTO(wallet);
    }

    //@Transactional
    public void delete(UUID id) {
        walletRepository.deleteById(id);
    }

}
