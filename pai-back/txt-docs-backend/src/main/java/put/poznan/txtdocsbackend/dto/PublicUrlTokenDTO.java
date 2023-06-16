package put.poznan.txtdocsbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicUrlTokenDTO {
    private String publicUrlToken;

    public PublicUrlTokenDTO(String publicUrlToken) {
        this.publicUrlToken = publicUrlToken;
    }
}
