package pl.error_handling_app.report;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.error_handling_app.user.User;

import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {

    public static Specification<Report> filterBy(String titleFragment, ReportStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (titleFragment != null && !titleFragment.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + titleFragment.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Report> filterByAssignedEmployee(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("assignedEmployee"), user);
    }

    public static Specification<Report> filterByReportingUser(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("reportingUser"), user);
    }
}

