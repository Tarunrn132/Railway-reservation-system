package com.railway.reservation.repository;

import com.railway.reservation.entity.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
    List<SmsLog> findByRecipientPhoneOrderByCreatedAtDesc(String phone);
}
