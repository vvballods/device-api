package com.ui.deviceapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ui.deviceapi.data.RegisterDeviceRequest;
import com.ui.deviceapi.data.TopologyNode;
import com.ui.deviceapi.data.TopologyResponse;
import com.ui.deviceapi.model.Device;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ui.deviceapi.common.TestUtils.getRandomMacAddress;
import static com.ui.deviceapi.errors.ApiErrorType.DEVICE_NOT_FOUND;
import static com.ui.deviceapi.errors.ApiErrorType.MAC_ADDRESS_ALREADY_IN_USE;
import static com.ui.deviceapi.model.DeviceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void shouldRegisterDevices() throws Exception {
        String macAddress = getRandomMacAddress();
        Device gateway = registerDevice(
                new RegisterDeviceRequest(macAddress, GATEWAY, null)
        );
        assertThat(gateway.getMacAddress()).isEqualTo(macAddress);
        assertThat(gateway.getDeviceType()).isEqualTo(GATEWAY);

        String switchMacAddress = getRandomMacAddress();
        Device switchDevice = registerDevice(
                new RegisterDeviceRequest(switchMacAddress, SWITCH, gateway.getMacAddress())
        );
        assertThat(switchDevice.getMacAddress()).isEqualTo(switchMacAddress);
        assertThat(switchDevice.getDeviceType()).isEqualTo(SWITCH);
        assertThat(switchDevice.getUplink()).isEqualTo(gateway);

        Device switchDeviceRead = getDeviceByMacAddress(switchDevice.getMacAddress());
        assertThat(switchDeviceRead).isEqualTo(switchDevice);

        String accessPointMacAddress = getRandomMacAddress();
        Device accessPoint = registerDevice(
                new RegisterDeviceRequest(accessPointMacAddress, ACCESS_POINT, switchDevice.getMacAddress())
        );
        assertThat(accessPoint.getMacAddress()).isEqualTo(accessPointMacAddress);
        assertThat(accessPoint.getDeviceType()).isEqualTo(ACCESS_POINT);
        assertThat(accessPoint.getUplink()).isEqualTo(switchDevice);

        List<Device> allDevices = getAllDevices();
        assertThat(allDevices.size()).isEqualTo(3);
        assertThat(allDevices).containsExactly(gateway, switchDevice, accessPoint);

        TopologyResponse topology = getTopology(null);
        assertThat(topology.roots().size()).isEqualTo(1);
        TopologyNode root = topology.roots().getFirst();
        assertThat(root.device()).isEqualTo(gateway);
        assertThat(root.children().stream().map(TopologyNode::device)).containsExactly(switchDevice);
        assertThat(root.children().getFirst().children().stream().map(TopologyNode::device)).containsExactly(accessPoint);
        assertThat(root.children().getFirst().children().getFirst().children()).isEmpty();

        TopologyResponse switchTopology = getTopology(switchMacAddress);
        assertThat(switchTopology.roots().size()).isEqualTo(1);
        root = switchTopology.roots().getFirst();
        assertThat(root.device()).isEqualTo(switchDevice);
        assertThat(root.children().stream().map(TopologyNode::device)).containsExactly(accessPoint);
        assertThat(root.children().getFirst().children()).isEmpty();
    }

    @Test
    @Transactional
    void shouldNotBeAbleRegisterDeviceWithSameMacAddress() throws Exception {
        RegisterDeviceRequest request = new RegisterDeviceRequest(getRandomMacAddress(), GATEWAY, null);
        registerDevice(request);
        MockHttpServletResponse response = mockMvc.perform(
                        post("/api/v1/devices/register")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                )
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(MAC_ADDRESS_ALREADY_IN_USE.getStatus().value());
        assertThat(response.getContentAsString()).contains(MAC_ADDRESS_ALREADY_IN_USE.getMessage());
    }

    @Test
    void shouldNotBeAbleRetrieveDeviceThatDoesNotExist() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        get("/api/v1/devices/" + getRandomMacAddress())
                                .contentType(APPLICATION_JSON)
                )
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(DEVICE_NOT_FOUND.getStatus().value());
        assertThat(response.getContentAsString()).contains(DEVICE_NOT_FOUND.getMessage());
    }

    @Test
    void shouldNotBeAbleToReferenceDeviceThatDoesNotExist() throws Exception {
        RegisterDeviceRequest request = new RegisterDeviceRequest(getRandomMacAddress(), SWITCH, getRandomMacAddress());
        MockHttpServletResponse response = mockMvc.perform(
                        post("/api/v1/devices/register")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                )
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(DEVICE_NOT_FOUND.getStatus().value());
        assertThat(response.getContentAsString()).contains(DEVICE_NOT_FOUND.getMessage());
    }

    @Test
    void shouldNotBeAbleToCreateTopologyForDeviceThatDoesNotExist() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        get("/api/v1/devices/topology")
                                .contentType(APPLICATION_JSON)
                                .param("macAddress", getRandomMacAddress())
                )
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(DEVICE_NOT_FOUND.getStatus().value());
        assertThat(response.getContentAsString()).contains(DEVICE_NOT_FOUND.getMessage());
    }

    @Test
    void shouldNotRetrieveTopologyIfNoDevicesExist() throws Exception {
        TopologyResponse topology = getTopology(null);
        assertThat(topology.roots()).isEmpty();
    }

    private Device registerDevice(RegisterDeviceRequest request) throws Exception {
        return objectMapper.readValue(mockMvc.perform(
                        post("/api/v1/devices/register")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                )
                .andReturn()
                .getResponse().getContentAsString(), Device.class);
    }

    private Device getDeviceByMacAddress(String macAddress) throws Exception {
        return objectMapper.readValue(mockMvc.perform(
                        get("/api/v1/devices/" + macAddress)
                                .contentType(APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString(), Device.class);
    }

    private List<Device> getAllDevices() throws Exception {
        return objectMapper.readValue(mockMvc.perform(
                        get("/api/v1/devices")
                                .contentType(APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Device.class));
    }

    private TopologyResponse getTopology(String macAddress) throws Exception {
        return objectMapper.readValue(mockMvc.perform(
                        get("/api/v1/devices/topology")
                                .contentType(APPLICATION_JSON)
                                .param("macAddress", macAddress)
                )
                .andReturn()
                .getResponse()
                .getContentAsString(), TopologyResponse.class);
    }


}