package com.bookkeeping.controller;

import com.bookkeeping.entity.Record;
import com.bookkeeping.service.RecordService;
import com.bookkeeping.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @GetMapping
    public Result<List<Record>> getUserRecords(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(recordService.getUserRecords(userId, startDate, endDate));
    }

    @PostMapping
    public Result<Record> createRecord(@RequestBody Record record) {
        return Result.success(recordService.createRecord(record));
    }

    @PutMapping("/{id}")
    public Result<Record> updateRecord(@PathVariable Long id, @RequestBody Record record) {
        record.setId(id);
        return Result.success(recordService.updateRecord(record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return Result.success(null);
    }
}
