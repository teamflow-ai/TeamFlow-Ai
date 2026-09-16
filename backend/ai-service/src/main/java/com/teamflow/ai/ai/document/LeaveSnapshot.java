package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * An approved leave window, built from {@code LeaveEvent} messages.
 *
 * <p>Only {@code leave.approved} is ever published, so presence in this collection
 * already means the leave is confirmed; the scorer uses it to discount an
 * employee's availability for dates the window covers.
 */
@Document(collection = "leave_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class LeaveSnapshot {

    @Id
    private String leaveRequestId;

    private String employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
