package jp.co.metateam.library.controller;
 
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
 
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
 
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;
 
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
 
import org.springframework.web.bind.annotation.RequestParam;
 
/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {
 
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
   
    @Autowired
    public RentalManageController(
    
        AccountService accountService,
        RentalManageService rentalManageService,
        StockService stockService) {

        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    
    }
 
    /**
     * 貸出一覧画面初期表示
     * @param model　//Modelオブジェクトが引数として渡されています。SpringMVCが提供するモデルオブジェクト。
     * @return //メソッドが返す値や戻り値に関する情報を提供するために使用される。つまりどういうこと？
     */
    
    @GetMapping("/rental/index")
    //メソッド名がindex、引数名がmodel。
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> RentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", RentalManageList);
        // 貸出一覧画面に遷移
    
        return "rental/index";
    }
   
 
    @GetMapping("/rental/add")
    public String add(Model model) {
 
        List <Stock> stockList = this.stockService.findAll();
        List <Account> accounts = this.accountService.findAll();
 
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
 
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
 
        return "rental/add";
    }
 
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
 
            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
 
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }
 
    /*　貸出編集画面 */
    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model, @RequestParam(name = "errorMessage", required = false) String errorMessage) {
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
 
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();
 
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
 
        model.addAttribute("rentalManageList", rentalManageList);
        model.addAttribute("rentalStockStatus", StockStatus.values());
 
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
 
        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            Long idLong = Long.parseLong(id);
            RentalManage rentalManage = this.rentalManageService.findById(idLong);
 
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
 
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
 
            model.addAttribute("rentalManageDto", rentalManageDto);
        }
 
        return "rental/edit";
    }
 

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, Model model) {
        try {
            // バリデーションエラーチェック
            if (result.hasErrors()) {
                model.addAttribute("errorMessage", "入力内容にエラーがあります");
                // バリデーションエラーがある場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
   
            // 貸出情報を取得
            RentalManage rentalManage = this.rentalManageService.findById(id);
            if (rentalManage == null) {
                model.addAttribute("errorMessage", "指定された貸出情報が見つかりません");
                // 貸出情報が見つからない場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
   
            // 貸出情報のステータスをチェック
            String statusErrorMessage = rentalManageDto.isValidStatus(rentalManage.getStatus());
            if (statusErrorMessage != null) {
                model.addAttribute("errorMessage", statusErrorMessage);
                // ステータスが無効な場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
   
            // 貸出予定日のバリデーションチェック
            if (rentalManage.getStatus() == RentalStatus.RENT_WAIT.getValue() &&rentalManageDto.getStatus() == RentalStatus.RENTAlING.getValue()){
            if (!rentalManageDto.isValidRentalDate()) {
       
                model.addAttribute("errorMessage", "貸出予定日は現在の日付に設定してください");
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
                }

            //返却予定日のバリデーションチェック
            }else if (rentalManage.getStatus() == RentalStatus.RENTAlING.getValue() &&rentalManageDto.getStatus() == RentalStatus.RETURNED.getValue()) {
            if(!rentalManageDto.isValidReturnDate()) {
       
                model.addAttribute("errorMessage", "返却予定日は現在の日付に設定してください");
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
                }
                
            }
            // 更新処理
            this.rentalManageService.update(id, rentalManageDto);
            return "redirect:/rental/index";
            } catch (Exception e) {
            // エラーが発生した場合の処理
            log.error("更新処理中にエラーが発生しました: " + e.getMessage());
            model.addAttribute("errorMessage", "更新処理中にエラーが発生しました");
            return "rental/edit";
        }
    }
}