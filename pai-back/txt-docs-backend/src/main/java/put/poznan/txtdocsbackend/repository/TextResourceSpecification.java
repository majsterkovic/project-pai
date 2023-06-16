package put.poznan.txtdocsbackend.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import put.poznan.txtdocsbackend.dto.FiltersDTO;
import put.poznan.txtdocsbackend.model.Tag;
import put.poznan.txtdocsbackend.model.TextResource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

public class TextResourceSpecification implements Specification<TextResource> {
    private FiltersDTO filters;

    public TextResourceSpecification(FiltersDTO filters) {
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(Root<TextResource> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters.getName() != null && !filters.getName().isEmpty()) {
            predicates.add(cb.like(root.get("name"), "%" + filters.getName() + "%"));
        }

        if (filters.getContent() != null && !filters.getContent().isEmpty()) {
            predicates.add(cb.like(root.get("content"), "%" + filters.getContent() + "%"));
        }

        if (filters.getTags() != null && !filters.getTags().isEmpty()) {
            Join<TextResource, Tag> join = root.join("tags");
            List<String> tagNames = filters.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            predicates.add(join.get("name").in(tagNames)); // użyj 'tagNames' zamiast 'filters.getTags()'
        }


        // Ensure that the resources returned belong to the user.
        if (filters.getUser() != null) {
            predicates.add(cb.equal(root.get("user"), filters.getUser()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
