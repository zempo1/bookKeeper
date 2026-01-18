package com.bookkeeping.repository;

import com.bookkeeping.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    List<Record> findByUserId(Long userId);
    List<Record> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
