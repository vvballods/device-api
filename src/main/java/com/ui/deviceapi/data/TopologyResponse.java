package com.ui.deviceapi.data;

import java.util.List;

public record TopologyResponse(
        List<TopologyNode> roots
) {
}
