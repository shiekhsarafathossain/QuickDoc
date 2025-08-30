package com.ewucsesummer25.quickdoc;

public class Doctor {

    private String name, username, email, password, address, phone, postalCode, bio, specialization, experience, qualification;
    private String doctorId;
    private String profileImageBase64;


    public Doctor() {
    }


    public Doctor(String name, String username, String email, String password, String address, String phone, String postalCode, String bio, String specialization, String experience, String qualification) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.postalCode = postalCode;
        this.bio = bio;
        this.specialization = specialization;
        this.experience = experience;
        this.qualification = qualification;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getExperience() {
        return experience;
    }


    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getQualification() {
        return qualification;
    }
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }
    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }
}

