package com.bookkeeping.service;

import com.bookkeeping.entity.Record;
import com.bookkeeping.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    public List<Record> getUserRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        return recordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate);
    }

    public Record createRecord(Record record) {
        return recordRepository.save(record);
    }

    public void deleteRecord(Long id) {
        recordRepository.deleteById(id);
    }
    
    public Record updateRecord(Record record) {
        return recordRepository.save(record);
    }
}
