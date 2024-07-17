package com.ui.deviceapi.repo;

import com.ui.deviceapi.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findDeviceByUplinkIsNull();

    List<Device> findDevicesByUplink(Device uplink);
}
