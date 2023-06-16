package put.poznan.txtdocsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import put.poznan.txtdocsbackend.model.Tag;


@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
}
