package com.finance.tracker.transactions.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unparsed_sms_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnparsedSmsLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    @Column(name = "sms_raw_body", columnDefinition = "TEXT")
    private String smsRawBody;

    @Column(name = "sms_timestamp")
    private Long timestamp;

    @Column(name = "error_reason")
    private String errorReason;
}
