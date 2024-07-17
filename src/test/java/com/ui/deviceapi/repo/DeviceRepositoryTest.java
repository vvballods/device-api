package com.ui.deviceapi.repo;

import com.ui.deviceapi.model.Device;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static com.ui.deviceapi.common.TestUtils.getRandomMacAddress;
import static com.ui.deviceapi.model.DeviceType.GATEWAY;
import static com.ui.deviceapi.model.DeviceType.SWITCH;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void shouldFindDevicesWithoutUplinkDevices() {
        assertThat(deviceRepository.findDeviceByUplinkIsNull()).isEmpty();

        Device gateway = new Device(getRandomMacAddress(), GATEWAY, null);
        deviceRepository.save(gateway);
        deviceRepository.save(new Device(getRandomMacAddress(), SWITCH, gateway));

        assertThat(deviceRepository.findDeviceByUplinkIsNull()).containsExactly(gateway);
    }

    @Test
    void shouldFindDevicesByUplinkDevice() {
        Device gateway = new Device(getRandomMacAddress(), GATEWAY, null);
        deviceRepository.save(gateway);

        assertThat(deviceRepository.findDevicesByUplink(gateway)).isEmpty();

        Device switchDevice = new Device(getRandomMacAddress(), SWITCH, gateway);
        deviceRepository.save(switchDevice);

        assertThat(deviceRepository.findDevicesByUplink(gateway)).containsExactly(switchDevice);
    }
}