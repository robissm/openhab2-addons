package org.openhab.binding.eldesalarm.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

public class EldesAlarmProvider implements InputReportListener, DeviceRemovalListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private AlarmType alarmType;
    private String login;
    private int pollingInterval;

    private EldesAlarmProviderListener listener;

    volatile private State state = State.NO_HID_DEVICE;
    volatile private boolean run = true;

    private final String LOGIN_CMD = "cfgpsw";
    private final String STATUS_CMD = "zstatus";

    private HidDevice dev;

    public EldesAlarmProvider(EldesAlarmProviderListener listener, String alarmType, String connectionType,
            String login, int pollingInterval) {
        this.listener = listener;
        this.alarmType = AlarmType.getAlarmType(alarmType);
        this.login = login;
        this.pollingInterval = pollingInterval;
    }

    public void start() {
        logger.info("start()");
        Thread fsmProcessThread = new Thread(this);
        fsmProcessThread.start();
    }

    public void stop() {
        logger.info("stop()");
        run = false;
    }

    @Override
    public void run() {
        // process fsm
        while (run) {
            synchronized (state) {
                switch (state) {
                    case NO_HID_DEVICE:
                        openHidDevice();
                        break;
                    case HID_DEVICE_OPEN:
                        sendLogin();
                        break;
                    case LOGED_IN:
                        requestZoneStatus();
                        break;
                    case ZONE_STATUS_PART_2_RECEIVED:
                        state = State.LOGED_IN;
                        try {
                            Thread.sleep(pollingInterval);
                        } catch (Exception ex) {
                            logger.warn("sleep interupted");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void openHidDevice() {
        logger.debug("openHidDevice()");
        state = State.OPENING_HID_DEVICE;
        try {
            HidDeviceInfo devInfo = null;
            logger.info("scanning for device");
            List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
            for (HidDeviceInfo info : devList) {
                if (info.getVendorId() == alarmType.getVendorId() && info.getProductId() == alarmType.getProductId()) {
                    devInfo = info;
                    break;
                }
            }
            if (devInfo == null) {
                logger.info("device not found");
                state = State.NO_HID_DEVICE;
                try {
                    Thread.sleep(3000);
                } catch (Exception ex) {
                    logger.warn("sleep interupted");
                }
                return;
            } else {
                logger.info("device found");
                dev = PureJavaHidApi.openDevice(devInfo);
            }
            state = State.HID_DEVICE_OPEN;
        } catch (Exception ex) {
            logger.warn("exception while opening device");
            logger.warn("Exception: ", ex);
            state = State.NO_HID_DEVICE;
            try {
                Thread.sleep(3000);
            } catch (Exception ex1) {
                logger.warn("sleep interupted");
            }
            return;
        }

        dev.setDeviceRemovalListener(this);
        dev.setInputReportListener(this);
    }

    private void sendLogin() {
        logger.debug("sendLogin()");
        state = State.LOGIN_REQUESTED;
        byte[] req = getRequestCommand(LOGIN_CMD, login);
        dev.setOutputReport((byte) 0x0, req, req.length);
    }

    private void requestZoneStatus() {
        logger.debug("requestZoneStatus()");
        state = State.ZONE_STATUS_REQUESTED;
        byte[] req = getRequestCommand(STATUS_CMD, null);
        dev.setOutputReport((byte) 0x0, req, req.length);
    }

    @Override
    public void onDeviceRemoval(HidDevice source) {
        logger.debug("onDeviceRemoval(source-{})", source);
        synchronized (state) {
            state = State.NO_HID_DEVICE;
        }
    }

    @Override
    public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
        if (data[0] != 0x00) {
            synchronized (state) {
                switch (state) {
                    case LOGIN_REQUESTED:
                        if (decodeResponseCommand(data).equals(LOGIN_CMD)
                                && decodeResponseMessage(data).equals("ok\r")) {
                            logger.info("successfull login response received");
                            state = State.LOGED_IN;
                        } else {
                            logger.warn("unsuccessfull login response received");
                            state = State.HID_DEVICE_OPEN;
                        }
                        break;
                    case ZONE_STATUS_REQUESTED:
                        String msg = decodeResponseMessage(data);
                        logger.debug("first part of zone status update received:{}", msg);
                        state = State.ZONE_STATUS_PART_1_RECEIVED;
                        updateZones1(msg);
                        break;
                    case ZONE_STATUS_PART_1_RECEIVED:
                        msg = decodeResponseMessage(data);
                        logger.debug("second part of zone status update received:{}", msg);
                        state = State.ZONE_STATUS_PART_2_RECEIVED;
                        updateZones2(msg);
                        break;
                    default:
                        logger.debug("unsupported state for zone status update");
                        break;
                }
            }
        }
    }

    private String decodeResponseMessage(byte[] response) {
        String message = "";
        try {
            message = new String(response);
            message = message.substring(message.indexOf(":") != -1 ? message.indexOf(":") + 1 : 1, response[0]);
            logger.debug("decoded message: {}", message);
            return message;
        } catch (Exception ex) {
            logger.warn("failed to parse response: {}", message);
            return null;
        }
    }

    private String decodeResponseCommand(byte[] response) {
        String message = "";
        try {
            message = new String(response);
            message = message.substring(1, message.indexOf(":") != -1 ? message.indexOf(":") : response.length);
            logger.debug("decoded response command: {}", message);
            return message;
        } catch (Exception ex) {
            logger.warn("failed to parse response command: {}", message);
            return null;
        }
    }

    private byte[] getRequestCommand(String command, String param) {
        String request = "";
        request += command;
        if (param != null && !param.isEmpty()) {
            request += ":" + param;
        }
        request += "\r";

        byte[] data = new byte[48];
        BigInteger bi = BigInteger.valueOf(request.length());
        data[0] = bi.toByteArray()[0];
        for (int i = 0; i < request.getBytes().length; i++) {
            data[i + 1] = request.getBytes()[i];
        }

        return data;
    }

    private void updateZones1(String response) {
        logger.debug("updateZones1(response-{})", response);

        // zone status is stored in hex encoded ints:
        // 0000000100000002 prased as 00000001 - first int, zones 1-32 and 00000000 - second int, zones 33-64
        // etc
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(DatatypeConverter.parseHexBinary(response.substring(8, 16)));
            outputStream.write(DatatypeConverter.parseHexBinary(response.substring(0, 8)));
        } catch (IOException e) {
        }
        BigInteger bb = new BigInteger(outputStream.toByteArray());
        for (int i = 0; i < 64; i++) {
            listener.onZoneStatusUpdated("Z" + Integer.toString(i + 1), bb.testBit(i));
        }
    }

    private void updateZones2(String response) {
        logger.debug("updateZones2(response-{})", response);

        // zone status is stored in hex encoded ints:
        // 00000001 first int, zones 65-96
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(DatatypeConverter.parseHexBinary(response.substring(0, 8)));
        } catch (IOException e) {
        }
        BigInteger bb = new BigInteger(outputStream.toByteArray());
        for (int i = 0; i < 32; i++) {
            listener.onZoneStatusUpdated("Z" + i + 65, bb.testBit(i));
        }
    }
}
