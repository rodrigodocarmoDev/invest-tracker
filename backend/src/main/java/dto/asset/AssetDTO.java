package dto.asset;

import java.util.UUID;


public record AssetDTO (
        UUID id,
        String ticker,
        String type,
        String name
) {}
