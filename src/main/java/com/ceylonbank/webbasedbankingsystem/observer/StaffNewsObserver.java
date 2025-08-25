package com.ceylonbank.webbasedbankingsystem.observer;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import com.ceylonbank.webbasedbankingsystem.entity.Notification;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.NotificationRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class StaffNewsObserver implements NewsObserver {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final List<String> STAFF_ROLES = Arrays.asList(
        "BankManager", "Cashier", "LoanOfficer", "CustomerServiceExecutive", "SystemAdministrator"
    );
    
    @Override
    public void onNewsPosted(BankNews news) {
        // Notify all staff for both public and private news
        for (String role : STAFF_ROLES) {
            List<User> staff = userRepository.findByRole_RoleName(role);
            
            for (User staffMember : staff) {
                Notification notification = new Notification();
                notification.setUserId(staffMember.getUserId());
                
                String message = news.getIsPublic() 
                    ? "New public news: " + news.getTitle()
                    : "New staff news: " + news.getTitle();
                    
                notification.setMessage(message);
                notification.setType("NEWS");
                notification.setIsRead(false);
                
                notificationRepository.save(notification);
            }
        }
    }
    
    @Override
    public String getObserverType() {
        return "STAFF";
    }
}
