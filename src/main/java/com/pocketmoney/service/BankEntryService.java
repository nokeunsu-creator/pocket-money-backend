package com.pocketmoney.service;

import com.pocketmoney.entity.BankEntry;
import com.pocketmoney.entity.BankEntryType;
import com.pocketmoney.repository.BankEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class BankEntryService {

    private final BankEntryRepository bankEntryRepository;

    public BankEntryService(BankEntryRepository bankEntryRepository) {
        this.bankEntryRepository = bankEntryRepository;
    }

    @Transactional
    public BankEntry createEntry(BankEntry entry) {
        return bankEntryRepository.save(entry);
    }

    @Transactional
    public BankEntry updateEntry(Long id, BankEntry updated) {
        BankEntry entry = bankEntryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("통장 기록을 찾을 수 없습니다: " + id));
        if (Boolean.TRUE.equals(entry.getDeleted())) {
            throw new NoSuchElementException("삭제된 기록은 수정할 수 없습니다: " + id);
        }
        entry.setType(updated.getType());
        entry.setAmount(updated.getAmount());
        entry.setCategory(updated.getCategory());
        entry.setMemo(updated.getMemo());
        entry.setEntryDate(updated.getEntryDate());
        return bankEntryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long id) {
        BankEntry entry = bankEntryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("통장 기록을 찾을 수 없습니다: " + id));
        if (Boolean.TRUE.equals(entry.getDeleted())) {
            return;
        }
        entry.setDeleted(true);
        entry.setDeletedAt(LocalDateTime.now());
        bankEntryRepository.save(entry);
    }

    public List<BankEntry> getEntriesByMonth(String userName, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return bankEntryRepository.findByUserNameAndDeletedFalseAndEntryDateBetweenOrderByEntryDateDescCreatedAtDesc(
                userName, start, end);
    }

    public List<BankEntry> getAllEntries(String userName) {
        return bankEntryRepository.findByUserNameAndDeletedFalseOrderByEntryDateDescCreatedAtDesc(userName);
    }

    public int getTotalBalance(String userName) {
        int deposit = bankEntryRepository.sumAmountByUserNameAndType(userName, BankEntryType.DEPOSIT);
        int withdraw = bankEntryRepository.sumAmountByUserNameAndType(userName, BankEntryType.WITHDRAW);
        return deposit - withdraw;
    }

    public Map<String, Object> getMonthlyStats(String userName, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        int monthDeposit = bankEntryRepository.sumAmountByUserNameAndTypeAndPeriod(
                userName, BankEntryType.DEPOSIT, start, end);
        int monthWithdraw = bankEntryRepository.sumAmountByUserNameAndTypeAndPeriod(
                userName, BankEntryType.WITHDRAW, start, end);
        int totalBalance = getTotalBalance(userName);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalBalance", totalBalance);
        stats.put("monthDeposit", monthDeposit);
        stats.put("monthWithdraw", monthWithdraw);

        return stats;
    }

    public List<BankEntry> getDeletedEntries(String userName) {
        return bankEntryRepository.findByUserNameAndDeletedTrueOrderByDeletedAtDesc(userName);
    }
}
