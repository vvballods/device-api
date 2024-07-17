package com.ui.deviceapi.controller;

import com.ui.deviceapi.data.RegisterDeviceRequest;
import com.ui.deviceapi.data.TopologyResponse;
import com.ui.deviceapi.model.Device;
import com.ui.deviceapi.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    public ResponseEntity<Device> registerDevice(@RequestBody RegisterDeviceRequest request) {
        return ResponseEntity.ok(deviceService.registerDevice(request));
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevicesSortedByType());
    }

    @GetMapping("/{macAddress}")
    public ResponseEntity<Device> getDevice(@PathVariable String macAddress) {
        return ResponseEntity.ok(deviceService.getDeviceByMacAddress(macAddress));
    }

    @GetMapping("/topology")
    public ResponseEntity<TopologyResponse> getTopology(@RequestParam(required = false) String macAddress) {
        if (macAddress != null && !macAddress.isBlank()) {
            return ResponseEntity.ok(deviceService.getTopology(macAddress));
        }
        return ResponseEntity.ok(deviceService.getTopology());
    }
}