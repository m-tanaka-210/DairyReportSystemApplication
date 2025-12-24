package com.techacademy.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 並び順を指定して全件検索
    List<Report> findAll(Sort sort);

    // 特定従業員に紐づいた日報を検索
    List<Report> findByEmployee(Employee employee, Sort sort);

    // 同一日付チェック
    boolean existsByEmployeeAndReportDateAndDeleteFlgFalse(Employee employee, java.time.LocalDate reportDate);
    boolean existsByEmployeeAndReportDateAndIdNotAndDeleteFlgFalse(
            Employee e, java.time.LocalDate d, Long id);
}