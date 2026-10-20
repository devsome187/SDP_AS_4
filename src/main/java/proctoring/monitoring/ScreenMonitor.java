package proctoring.monitoring;

public class ScreenMonitor {

    private boolean online = true;

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void startCapture(String studentId) {
        if (!online) {
            throw new ScreenMonitorUnavailableException("Screen monitor is offline for student " + studentId);
        }
    }
}
