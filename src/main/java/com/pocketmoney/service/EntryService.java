package com.pocketmoney.service;

import com.pocketmoney.entity.Entry;
import com.pocketmoney.entity.EntryType;
import com.pocketmoney.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class EntryService {

    private final EntryRepository entryRepository;

    public EntryService(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    /** 기록 추가 */
    @Transactional
    public Entry createEntry(Entry entry) {
        return entryRepository.save(entry);
    }

    /** 기록 수정 */
    @Transactional
    public Entry updateEntry(Long id, Entry updated) {
        Entry entry = entryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("기록을 찾을 수 없습니다: " + id));
        entry.setType(updated.getType());
        entry.setAmount(updated.getAmount());
        entry.setCategory(updated.getCategory());
        entry.setMemo(updated.getMemo());
        entry.setEntryDate(updated.getEntryDate());
        return entryRepository.save(entry);
    }

    /** 기록 삭제 */
    @Transactional
    public void deleteEntry(Long id) {
        entryRepository.deleteById(id);
    }

    /** 월별 기록 조회 */
    public List<Entry> getEntriesByMonth(String userName, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return entryRepository.findByUserNameAndEntryDateBetweenOrderByEntryDateDescCreatedAtDesc(
                userName, start, end);
    }

    /** 전체 기록 조회 */
    public List<Entry> getAllEntries(String userName) {
        return entryRepository.findByUserNameOrderByEntryDateDescCreatedAtDesc(userName);
    }

    /** 전체 잔액 (총수입 - 총지출) */
    public int getTotalBalance(String userName) {
        int income = entryRepository.sumAmountByUserNameAndType(userName, EntryType.INCOME);
        int expense = entryRepository.sumAmountByUserNameAndType(userName, EntryType.EXPENSE);
        return income - expense;
    }

    /** 월별 통계 */
    public Map<String, Object> getMonthlyStats(String userName, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        int monthIncome = entryRepository.sumAmountByUserNameAndTypeAndPeriod(
                userName, EntryType.INCOME, start, end);
        int monthExpense = entryRepository.sumAmountByUserNameAndTypeAndPeriod(
                userName, EntryType.EXPENSE, start, end);
        int totalBalance = getTotalBalance(userName);

        // 카테고리별 지출
        List<Object[]> expenseByCategory = entryRepository.sumByCategoryAndPeriod(
                userName, EntryType.EXPENSE, start, end);
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (Object[] row : expenseByCategory) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", row[0]);
            item.put("amount", ((Number) row[1]).intValue());
            categoryStats.add(item);
        }

        // 카테고리별 수입
        List<Object[]> incomeByCategory = entryRepository.sumByCategoryAndPeriod(
                userName, EntryType.INCOME, start, end);
        List<Map<String, Object>> incomeCategoryStats = new ArrayList<>();
        for (Object[] row : incomeByCategory) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", row[0]);
            item.put("amount", ((Number) row[1]).intValue());
            incomeCategoryStats.add(item);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalBalance", totalBalance);
        stats.put("monthIncome", monthIncome);
        stats.put("monthExpense", monthExpense);
        stats.put("expenseByCategory", categoryStats);
        stats.put("incomeByCategory", incomeCategoryStats);

        return stats;
    }
}
