package put.poznan.txtdocsbackend.model;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class TextResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String name;

    private String content;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Tag> tags;

    private String publicUrlToken;

    private String imagePath;
}
