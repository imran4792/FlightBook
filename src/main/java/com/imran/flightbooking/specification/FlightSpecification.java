package com.imran.flightbooking.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.imran.flightbooking.entity.Flight;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class FlightSpecification {

    public static Specification<Flight> buildFilter(
            String keyword,
            Long airlineId,
            String source,
            String destination,
            String status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("flightNumber")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.join("airline", JoinType.LEFT).get("airlineName")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("source")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("destination")), likeKeyword)));
            }

            if (airlineId != null) {
                predicates.add(criteriaBuilder.equal(root.join("airline", JoinType.LEFT).get("airlineId"), airlineId));
            }

            if (StringUtils.hasText(source)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("source")), source.trim().toLowerCase()));
            }

            if (StringUtils.hasText(destination)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("destination")), destination.trim().toLowerCase()));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.trim().toLowerCase()));
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
