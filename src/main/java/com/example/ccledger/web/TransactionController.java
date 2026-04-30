package com.example.ccledger.web;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
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
	  if (search.getPaid() == null) {
		    search.setPaid(false);
		  }
	  
	  
    var spec = org.springframework.data.jpa.domain.Specification
        .where(TransactionSpecification.billingMonthEquals(search.getBillingMonth()))
        .and(TransactionSpecification.cardEquals(search.getCardId()))
        .and(TransactionSpecification.paidEquals(search.getPaid()))
        .and(TransactionSpecification.itemNameLike(search.getKeyword()));

    model.addAttribute(
    	    "rows",
    	    txRepo.findAll(
    	        spec,
    	        Sort.by(
    	            Sort.Order.desc("purchaseDate"),
    	            Sort.Order.desc("id")
    	        )
    	    )
    	);

    // --- 画面の選択肢 ---
    model.addAttribute("cards", cardRepo.findAll());
    model.addAttribute("months", monthOptions(24));
    TransactionForm form = new TransactionForm();
    form.setPurchaseDate(java.time.LocalDate.now());         // 今日
    form.setBillingMonth(YearMonth.now().toString());        // 当月

    model.addAttribute("form", form);
    // --- 以前の「当月から2か月先まで」の集計 ---
    String m0 = YearMonth.now().toString();               // 当月 yyyy-MM
    String m1 = YearMonth.now().plusMonths(1).toString(); // 当月+1 yyyy-MM
    String m2 = YearMonth.now().plusMonths(2).toString(); // 当月+2 yyyy-MM


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
      
    //グラフ作成
    String chartStart = YearMonth.now().minusMonths(11).toString(); // 12か月前を含む
    String chartEnd = YearMonth.now().toString();
    
      var paidSummaryRows = txRepo.findPaidMonthlySummary(chartStart, chartEnd);

      var paidMap = new java.util.HashMap<String, Long>();
      for (var p : paidSummaryRows) {
        paidMap.put(p.getPaidMonth(), p.getTotalAmount());
      }

      List<String> chartLabels = new ArrayList<>();
      List<Long> chartValues = new ArrayList<>();

      for (int i = 11; i >= 0; i--) {
        String month = YearMonth.now().minusMonths(i).toString();
        chartLabels.add(month);
        chartValues.add(paidMap.getOrDefault(month, 0L));
      }

    //HTML側で ${summary} として使えるようにする
    model.addAttribute("summary", summary);
    model.addAttribute("currentMonth", m0);

    model.addAttribute("chartLabels", chartLabels);
    model.addAttribute("chartValues", chartValues);
    
    return "transactions/list";
  }



//画面から送られた入力値を受け取って、DBに保存する処理
  @PostMapping
                             //フォーム値を受け取る
  public String create(@Valid @ModelAttribute("form") TransactionForm form,
                       BindingResult br,
                       Model model) {
	//必須値
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

	//支払済み・未払いの切り替え
  @PostMapping("/{id}/toggle-paid")
  public String togglePaid(@PathVariable Long id) {
    Transaction tx = txRepo.findById(id).orElseThrow();
    tx.setPaid(!tx.isPaid());
    normalizePaid(tx, tx.getPaidMonth());
    txRepo.save(tx);
    return "redirect:/transactions";
  }
	//レコード削除
  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id) {
    txRepo.deleteById(id);
    return "redirect:/transactions";
  }
//支払済みなら、
//支払月が入力されていればその値を入れる
//入力されていなければ請求月を入れる
//未払いなら支払い月を消す
//
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
    // 現在月を中心に、2年後まで
    YearMonth now = YearMonth.now();
    List<String> list = new ArrayList<>();
    for (int i = -12; i <= monthsRange; i++) {
      list.add(now.plusMonths(i).toString()); // yyyy-MM
    }
    return list;
  }
  //編集する
  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    Transaction tx = txRepo.findById(id).orElseThrow();

    TransactionForm form = new TransactionForm();
    form.setItemName(tx.getItemName());
    form.setCardId(tx.getCard().getId());
    form.setAmount(tx.getAmount());
    form.setPurchaseDate(tx.getPurchaseDate());
    form.setBillingMonth(tx.getBillingMonth());
    form.setPaidMonth(tx.getPaidMonth());
    form.setPaid(tx.isPaid());

    model.addAttribute("transactionId", id);
    model.addAttribute("form", form);
    model.addAttribute("cards", cardRepo.findAll());
    model.addAttribute("months", monthOptions(12));

    return "transactions/edit";
  }
  @PostMapping("/{id}/edit")
  public String update(@PathVariable Long id,
                       @Valid @ModelAttribute("form") TransactionForm form,
                       BindingResult br,
                       Model model) {
    if (br.hasErrors()) {
      model.addAttribute("transactionId", id);
      model.addAttribute("cards", cardRepo.findAll());
      model.addAttribute("months", monthOptions(12));
      return "transactions/edit";
    }

    Transaction tx = txRepo.findById(id).orElseThrow();
    Card card = cardRepo.findById(form.getCardId()).orElseThrow();

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
}
