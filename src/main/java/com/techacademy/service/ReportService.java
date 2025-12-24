package com.techacademy.service;

import java.time.LocalDate;
import java.util.List;

import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "reportDate", "id");

    // 日報一覧
    public List<Report> listFor(Employee me, boolean isAdmin) {
        if (isAdmin) {
            return reportRepository.findAll(DEFAULT_SORT);
        } else {
            return reportRepository.findByEmployee(me, DEFAULT_SORT);
        }
    }
    // 1件取得（なければnull）
    public Report find(Integer id) {
        Optional<Report> op = reportRepository.findById(id.longValue());
        return op.orElse(null);
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {
        if (report.getId() == null) {
            report.setDeleteFlg(false);
        }
        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 同一日付チェック
    public boolean existsSameDate(Employee e, LocalDate d) {
        return reportRepository.existsByEmployeeAndReportDateAndDeleteFlgFalse(e, d);
    }

    public boolean existsSameDateExceptId(Employee e, LocalDate d, Long excludeId) {
        return reportRepository.existsByEmployeeAndReportDateAndIdNotAndDeleteFlgFalse(e, d, excludeId);
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(Integer id) {
        Report r = find(id);
        if (r == null) {
            return ErrorKinds.NOT_FOUND_ERROR; // 無ければエラーメッセージを表示
        }
        r.setDeleteFlg(true);
        reportRepository.save(r);
        return ErrorKinds.SUCCESS;
    }
}