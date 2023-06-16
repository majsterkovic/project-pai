package put.poznan.txtdocsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import put.poznan.txtdocsbackend.model.TextResource;
import put.poznan.txtdocsbackend.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TextResourceRepository extends JpaRepository<TextResource, Long>, JpaSpecificationExecutor<TextResource> {

    List<TextResource> findAllByUser(User user);

    Optional<TextResource> findByNameAndUser(String name, User user);


    Optional<TextResource> findByIdAndPublicUrlToken(Long id, String publicUrlToken);

    Optional<TextResource> findByIdAndUser(Long id, User user);
}
