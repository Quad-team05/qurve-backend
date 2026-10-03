package com.qurve.expression.repository;

import com.qurve.expression.domain.BasicExpression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasicExpressionRepository extends JpaRepository<BasicExpression, Long> {

    List<BasicExpression> findAllByOrderByOrderNumberAsc();
}