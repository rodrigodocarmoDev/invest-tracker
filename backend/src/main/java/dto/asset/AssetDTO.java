package dto.asset;

import java.util.UUID;

@AllArgsContructor
@NoArgsConstructor
@Setter
@Getter
public class AssetDTO {

    private UUID id;
    private String ticker;
    private String type;
    private String name;

}
