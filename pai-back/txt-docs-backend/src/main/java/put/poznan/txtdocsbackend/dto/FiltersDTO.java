package put.poznan.txtdocsbackend.dto;

import lombok.Getter;
import lombok.Setter;
import put.poznan.txtdocsbackend.model.Tag;
import put.poznan.txtdocsbackend.model.User;

import java.util.List;

@Getter
@Setter
public class FiltersDTO {

    private User user;
    private String name;
    private String content;
    private List<Tag> tags;

    public FiltersDTO() {
    }

    public FiltersDTO(User user, String name, String content, List<Tag> tags) {
        this.user = user;
        this.name = name;
        this.content = content;
        this.tags = tags;
    }
}
