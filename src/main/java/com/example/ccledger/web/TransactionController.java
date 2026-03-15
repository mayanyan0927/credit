package com.example.ccledger.web;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ccledger.domain.Card;
import com.example.ccledger.domain.Transaction;
import com.example.ccledger.repository.CardRepository;
import com.example.ccledger.repository.TransactionRepository;
import com.example.ccledger.repository.TransactionSpecification;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

  private final TransactionRepository txRepo;
  private final CardRepository cardRepo;

  public TransactionController(TransactionRepository txRepo, CardRepository cardRepo) {
    this.txRepo = txRepo;
    this.cardRepo = cardRepo;
  }

  @GetMapping
  public String list(
      @ModelAttribute("search") TransactionSearchForm search,
      Model model
  ) {
    // --- 検索結果（一覧） ---
    var spec = org.springframework.data.jpa.domain.Specification
        .where(TransactionSpecification.billingMonthEquals(search.getBillingMonth()))
        .and(TransactionSpecification.cardEquals(search.getCardId()))
        .and(TransactionSpecification.paidEquals(search.getPaid()))
        .and(TransactionSpecification.itemNameLike(search.getKeyword()));

    model.addAttribute("rows", txRepo.findAll(spec));

    // --- 画面の選択肢 ---
    model.addAttribute("cards", cardRepo.findAll());
    model.addAttribute("months", monthOptions(24));
    model.addAttribute("form", new TransactionForm());

    // --- 以前の「当月から2か月先まで」の集計（復活） ---
    String m0 = YearMonth.now().toString();           // 当月 yyyy-MM
    String m1 = YearMonth.now().plusMonths(1).toString();
    String m2 = YearMonth.now().plusMonths(2).toString();

    var summaryRows = txRepo.findMonthlySummary(m0, m2);

    var map = new java.util.HashMap<String, TransactionRepository.MonthlySummary>();
    for (var s : summaryRows) map.put(s.getBillingMonth(), s);

    record MonthBox(String month, long total, long unpaid) {}
    var summary = new java.util.ArrayList<MonthBox>();
    for (String m : java.util.List.of(m0, m1, m2)) {
      var s = map.get(m);
      long total = (s == null || s.getTotalAmount() == null) ? 0 : s.getTotalAmount();
      long unpaid = (s == null || s.getUnpaidAmount() == null) ? 0 : s.getUnpaidAmount();
      summary.add(new MonthBox(m, total, unpaid));
    }

    model.addAttribute("summary", summary);
    model.addAttribute("currentMonth", m0);

    return "transactions/list";
  }




  @PostMapping
  public String create(@Valid @ModelAttribute("form") TransactionForm form,
                       BindingResult br,
                       Model model) {
    if (br.hasErrors()) {
      model.addAttribute("rows", txRepo.findAll());
      model.addAttribute("cards", cardRepo.findAll());
      model.addAttribute("months", monthOptions(24));
      return "transactions/list";
    }

    Card card = cardRepo.findById(form.getCardId()).orElseThrow();

    Transaction tx = new Transaction();
    tx.setItemName(form.getItemName());
    tx.setCard(card);
    tx.setAmount(form.getAmount());
    tx.setPurchaseDate(form.getPurchaseDate());
    tx.setBillingMonth(form.getBillingMonth());
    tx.setPaid(form.isPaid());

    normalizePaid(tx, form.getPaidMonth());

    txRepo.save(tx);
    return "redirect:/transactions";
  }

  @PostMapping("/{id}/toggle-paid")
  public String togglePaid(@PathVariable Long id) {
    Transaction tx = txRepo.findById(id).orElseThrow();
    tx.setPaid(!tx.isPaid());
    normalizePaid(tx, tx.getPaidMonth());
    txRepo.save(tx);
    return "redirect:/transactions";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id) {
    txRepo.deleteById(id);
    return "redirect:/transactions";
  }

  private void normalizePaid(Transaction tx, String paidMonthInput) {
    if (tx.isPaid()) {
      tx.setPaidMonth(paidMonthInput != null && !paidMonthInput.isBlank()
        ? paidMonthInput
        : tx.getBillingMonth());
    } else {
      tx.setPaidMonth(null);
    }
  }

  private List<String> monthOptions(int monthsRange) {
    // 現在月を中心に、過去monthsRange/未来monthsRange
    YearMonth now = YearMonth.now();
    List<String> list = new ArrayList<>();
    for (int i = -monthsRange; i <= monthsRange; i++) {
      list.add(now.plusMonths(i).toString()); // yyyy-MM
    }
    return list;
  }
}
