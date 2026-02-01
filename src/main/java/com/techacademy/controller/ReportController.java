package com.techacademy.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.techacademy.entity.Employee;

import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;

    @Autowired
    public ReportController(ReportService reportService, EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
    }

    // ログイン中の従業員エンティティを取得
    private Employee loginEmployee(Principal principal) {
        // ユーザー名に社員番号が入っている前提
        return employeeService.findByCode(principal.getName());
    }

    // 管理者かどうかの判定
    private boolean isAdmin(Employee e) {
        return e.getRole().toString().equals("ADMIN");
    }
    // 日報一覧画面
    @GetMapping
    public String list(Model model, Principal principal) {
        // ログイン従業員
        Employee me = loginEmployee(principal);
        // 一覧取得
        List<Report> reports = reportService.listFor(me, isAdmin(me));
        model.addAttribute("reports", reports);
        model.addAttribute("listSize", reports.size());
        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, Model model) {
        Report r = reportService.find(id);
        if (r == null) { return "redirect:/reports"; }
        model.addAttribute("report", r);
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, Principal principal) {
        report.setEmployee(loginEmployee(principal));
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated @ModelAttribute Report report, BindingResult res, Principal principal) {
        Employee me = loginEmployee(principal);
        report.setEmployee(me);

        // エラー時は画面に戻す
        if (res.hasErrors()) {
            report.setEmployee(loginEmployee(principal));
            return "reports/new";
        }
        // 同一日付（ログイン者×日付）チェック
        if (reportService.existsSameDate(me, report.getReportDate())) {
            res.rejectValue("reportDate", null, "既に登録されている日付です。");
            return "reports/new";
        }

        // セキュリティ上、employeeはログイン者で上書きする
        report.setEmployee(loginEmployee(principal));
        reportService.save(report);
        return "redirect:/reports";
    }

    // 日報表示
    @GetMapping("/{id}/update")
    public String edit(@PathVariable Integer id, Model model, Principal principal) {
        Report r = reportService.find(id);
        if (r == null) { return "redirect:/reports";
        }
        Employee me = loginEmployee(principal);
        if (!isAdmin(me) && !r.getEmployee().getCode().equals(me.getCode())) {
            return "redirect:/reports";
        }
        model.addAttribute("report", r);
        return "reports/edit";
    }

    // 日報更新
    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id,
                         @Validated @ModelAttribute("report") Report form,
                         BindingResult br, Principal principal, Model model) {
        Report r = reportService.find(id);
        if (r == null) return "redirect:/reports";

        // 権限チェック
        Employee me = loginEmployee(principal);
        if (!isAdmin(me) && !r.getEmployee().getCode().equals(me.getCode())) {
            return "redirect:/reports";
        }

        // エラー時は画面に戻す
        if (br.hasErrors()){
            form.setEmployee(r.getEmployee());
            model.addAttribute("report", form);
            return "reports/edit";
        }

        // 同一日付（ログイン者×日付）チェック
        if (reportService.existsSameDateExceptId(r.getEmployee(), form.getReportDate(), r.getId())) {
            br.rejectValue("reportDate", null, "既に登録されている日付です。");
            form.setEmployee(r.getEmployee());
            model.addAttribute("report", form);
            return "reports/edit";
        }

        r.setReportDate(form.getReportDate());
        r.setTitle(form.getTitle());
        r.setContent(form.getContent());
        reportService.save(r);
        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, Principal principal) {
        Report r = reportService.find(id);
        if (r != null) {
            Employee me = loginEmployee(principal);
            if (isAdmin(me) || r.getEmployee().getCode().equals(me.getCode())) {
                reportService.delete(id);
            }
        }
        return "redirect:/reports";
    }

}
