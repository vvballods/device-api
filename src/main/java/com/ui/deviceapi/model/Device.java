package com.ui.deviceapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Device {

    @Id
    private String macAddress;

    @Column(nullable = false)
    private DeviceType deviceType;

    @ManyToOne
    @JoinColumn(name = "uplink_mac_address")
    private Device uplink;
}

