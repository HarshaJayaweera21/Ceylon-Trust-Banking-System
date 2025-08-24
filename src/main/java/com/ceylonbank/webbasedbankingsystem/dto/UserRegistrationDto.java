package com.ceylonbank.webbasedbankingsystem.dto;

public class UserRegistrationDto {
    private String firstName;
    private String lastName;
    private String email;
    private String nic;
    private String dob;         // matches getDob()
    private String street;      // matches getStreet()
    private String street2;
    private String city;
    private String postalCode;  // matches getPostalCode()
    private String phone;       // matches getPhone()
    private String phoneType;   // if used
    private String username;
    private String password;
    private String securityQuestion1;
    private String securityAnswer1;
    private String securityQuestion2;
    private String securityAnswer2;

    // Add getters and setters for each field:

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getStreet2() { return street2; }
    public void setStreet2(String street2) { this.street2 = street2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhoneType() { return phoneType; }
    public void setPhoneType(String phoneType) { this.phoneType = phoneType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSecurityQuestion1() { return securityQuestion1; }
    public void setSecurityQuestion1(String securityQuestion1) { this.securityQuestion1 = securityQuestion1; }

    public String getSecurityAnswer1() { return securityAnswer1; }
    public void setSecurityAnswer1(String securityAnswer1) { this.securityAnswer1 = securityAnswer1; }

    public String getSecurityQuestion2() { return securityQuestion2; }
    public void setSecurityQuestion2(String securityQuestion2) { this.securityQuestion2 = securityQuestion2; }

    public String getSecurityAnswer2() { return securityAnswer2; }
    public void setSecurityAnswer2(String securityAnswer2) { this.securityAnswer2 = securityAnswer2; }
}
