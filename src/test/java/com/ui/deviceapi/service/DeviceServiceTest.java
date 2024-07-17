package com.ui.deviceapi.service;

import com.ui.deviceapi.data.RegisterDeviceRequest;
import com.ui.deviceapi.model.Device;
import com.ui.deviceapi.repo.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static com.ui.deviceapi.common.TestUtils.getRandomMacAddress;
import static com.ui.deviceapi.model.DeviceType.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeviceService.class)
class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;
    @MockBean
    private DeviceRepository deviceRepository;

    @Test
    void testDeviceRegistration() {
        RegisterDeviceRequest request = new RegisterDeviceRequest(getRandomMacAddress(), GATEWAY, null);

        deviceService.registerDevice(request);

        verify(deviceRepository, times(1)).save(new Device(request.macAddress(), request.deviceType(), null));
    }

    @Test
    void testDeviceTopology() {
        Device gateway = new Device(getRandomMacAddress(), GATEWAY, null);
        Device switchDevice = new Device(getRandomMacAddress(), SWITCH, gateway);
        Device accessPoint = new Device(getRandomMacAddress(), ACCESS_POINT, switchDevice);
        when(deviceRepository.findDeviceByUplinkIsNull()).thenReturn(List.of(gateway));
        when(deviceRepository.findDevicesByUplink(gateway)).thenReturn(List.of(switchDevice));
        when(deviceRepository.findDevicesByUplink(switchDevice)).thenReturn(List.of(accessPoint));

        deviceService.getTopology();

        verify(deviceRepository, times(1)).findDeviceByUplinkIsNull();
        verify(deviceRepository, times(1)).findDevicesByUplink(gateway);
        verify(deviceRepository, times(1)).findDevicesByUplink(switchDevice);
        verify(deviceRepository, times(1)).findDevicesByUplink(accessPoint);
    }

    @Test
    void testDeviceTopologyForSpecificDevice() {
        Device gateway = new Device(getRandomMacAddress(), GATEWAY, null);
        Device switchDevice = new Device(getRandomMacAddress(), SWITCH, gateway);
        Device accessPoint = new Device(getRandomMacAddress(), ACCESS_POINT, switchDevice);
        when(deviceRepository.findById(switchDevice.getMacAddress())).thenReturn(Optional.of(switchDevice));
        when(deviceRepository.findDevicesByUplink(gateway)).thenReturn(List.of(switchDevice));
        when(deviceRepository.findDevicesByUplink(switchDevice)).thenReturn(List.of(accessPoint));

        deviceService.getTopology(switchDevice.getMacAddress());

        verify(deviceRepository, never()).findDeviceByUplinkIsNull();
        verify(deviceRepository, never()).findDevicesByUplink(gateway);
        verify(deviceRepository, times(1)).findDevicesByUplink(switchDevice);
        verify(deviceRepository, times(1)).findDevicesByUplink(accessPoint);
    }
}