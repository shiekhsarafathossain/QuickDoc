package com.ewucsesummer25.quickdoc;

public class Appointment {
    private String appointmentId, patientId, doctorId;
    private String doctorName, patientName;
    private String appointmentDate;
    private String appointmentTime;
    private String status;


    public Appointment() {
    }

    public Appointment(String appointmentId, String patientId, String doctorId, String doctorName, String patientName, String appointmentDate, String appointmentTime, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public String getAppointmentId() {

        return appointmentId;
    }
    public void setAppointmentId(String appointmentId) {

        this.appointmentId = appointmentId;
    }

    public String getPatientId() {

        return patientId;
    }
    public void setPatientId(String patientId) {

        this.patientId = patientId;
    }



    public String getDoctorId() {

        return doctorId;
    }
    public void setDoctorId(String doctorId) {

        this.doctorId = doctorId;
    }

    public String getDoctorName() {

        return doctorName;
    }
    public void setDoctorName(String doctorName) {

        this.doctorName = doctorName;
    }

    public String getPatientName() {

        return patientName;
    }
    public void setPatientName(String patientName) {

        this.patientName = patientName;
    }

    public String getAppointmentDate() {

        return appointmentDate;
    }
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {

        return appointmentTime;
    }
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {

        return status;
    }
    public void setStatus(String status) {

        this.status = status;
    }
}
