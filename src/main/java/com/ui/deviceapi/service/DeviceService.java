package com.ui.deviceapi.service;

import com.ui.deviceapi.data.RegisterDeviceRequest;
import com.ui.deviceapi.data.TopologyNode;
import com.ui.deviceapi.data.TopologyResponse;
import com.ui.deviceapi.errors.ApiException;
import com.ui.deviceapi.model.Device;
import com.ui.deviceapi.repo.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.ui.deviceapi.errors.ApiErrorType.DEVICE_NOT_FOUND;
import static com.ui.deviceapi.errors.ApiErrorType.MAC_ADDRESS_ALREADY_IN_USE;
import static java.util.stream.Collectors.toList;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device registerDevice(RegisterDeviceRequest request) {
        if (deviceRepository.findById(request.macAddress()).isPresent()) {
            throw new ApiException(MAC_ADDRESS_ALREADY_IN_USE);
        }

        Device device = new Device();
        device.setMacAddress(request.macAddress());
        device.setDeviceType(request.deviceType());

        if (request.uplinkMacAddress() != null) {
            Optional<Device> uplinkDevice = deviceRepository.findById(request.uplinkMacAddress());
            if (uplinkDevice.isEmpty()) {
                throw new ApiException(DEVICE_NOT_FOUND);
            }
            uplinkDevice.ifPresent(device::setUplink);
        }

        return deviceRepository.save(device);
    }

    public List<Device> getAllDevicesSortedByType() {
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(Device::getDeviceType))
                .collect(toList());
    }

    public Device getDeviceByMacAddress(String macAddress) {
        return deviceRepository.findById(macAddress)
                .orElseThrow(() -> new ApiException(DEVICE_NOT_FOUND));
    }

    public TopologyResponse getTopology() {
        List<TopologyNode> roots = deviceRepository.findDeviceByUplinkIsNull().stream()
                .map(this::buildTopologyTree)
                .collect(toList());
        return new TopologyResponse(roots);
    }

    public TopologyResponse getTopology(String macAddress) {
        TopologyNode root = buildTopologyTree(getDeviceByMacAddress(macAddress));
        return new TopologyResponse(List.of(root));
    }

    private TopologyNode buildTopologyTree(Device currentDevice) {
        List<TopologyNode> children = deviceRepository.findDevicesByUplink(currentDevice).stream()
                .map(this::buildTopologyTree)
                .collect(toList());
        return new TopologyNode(currentDevice, children);
    }
}
