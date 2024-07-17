package com.ui.deviceapi.data;

import com.ui.deviceapi.model.DeviceType;

public record RegisterDeviceRequest(
        String macAddress,
        DeviceType deviceType,
        String uplinkMacAddress
) {
}
