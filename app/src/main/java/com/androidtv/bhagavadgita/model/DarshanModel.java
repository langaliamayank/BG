package com.androidtv.bhagavadgita.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DarshanModel implements Serializable {
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("titleHi")
    @Expose
    private String titleHi;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("startTime")
    @Expose
    private String startTime;
    @SerializedName("endTime")
    @Expose
    private String endTime;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("messageHi")
    @Expose
    private String messageHi;
    @SerializedName("background")
    @Expose
    private String background;

    public LocalTime lTstartTime;
    public LocalTime lTendTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleHi() {
        return titleHi;
    }

    public void setTitleHi(String titleHi) {
        this.titleHi = titleHi;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageHi() {
        return messageHi;
    }

    public void setMessageHi(String messageHi) {
        this.messageHi = messageHi;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public boolean isOpen() {
        return status != null && status.startsWith("Open");
    }

    public boolean isUpcoming() {
        return "Upcoming".equals(status);
    }

    public boolean isClosed() {
        return "Closed".equals(status);
    }

    public void updateStatus(LocalTime now) {
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            status = "N/A";
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

        try {
            // Normalize AM/PM in case of lowercase input
            this.lTstartTime = LocalTime.parse(startTime.toUpperCase(), formatter);
            this.lTendTime = LocalTime.parse(endTime.toUpperCase(), formatter);

            if (now.isBefore(lTstartTime)) {
                status = "Upcoming";
            } else if (!now.isBefore(lTstartTime) && now.isBefore(lTendTime)) {
                int minutesLeft = (int) Duration.between(now, lTendTime).toMinutes();
                status = "Open (" + minutesLeft + " Min Left)";
            } else {
                status = "Closed";
            }
        } catch (Exception e) {
            // If parsing fails (bad format), mark as N/A
            status = "N/A";
        }
    }

    public static DarshanModel getActiveTheme(List<DarshanModel> darshanList) {
        if (darshanList == null || darshanList.isEmpty()) return null;

        for (DarshanModel d : darshanList) {
            if (d.isOpen()) return d;
        }
        for (DarshanModel d : darshanList) {
            if (d.isUpcoming()) return d;
        }
        return darshanList.get(darshanList.size() - 1);
    }

    @Override
    public String toString() {
        return "DarshanModel{" +
                "title='" + title + '\'' +
                ", titleHi='" + titleHi + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", messageHi='" + messageHi + '\'' +
                ", background='" + background + '\'' +
                ", lTstartTime=" + lTstartTime +
                ", lTendTime=" + lTendTime +
                '}';
    }
}
