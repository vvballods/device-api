package com.ui.deviceapi.data;

import com.ui.deviceapi.model.Device;

import java.util.List;

public record TopologyNode(
        Device device,
        List<TopologyNode> children
) {
}

