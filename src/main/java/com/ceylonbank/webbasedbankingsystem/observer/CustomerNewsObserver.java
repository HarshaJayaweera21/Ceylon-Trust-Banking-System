package com.ceylonbank.webbasedbankingsystem.observer;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import com.ceylonbank.webbasedbankingsystem.entity.Notification;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.NotificationRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerNewsObserver implements NewsObserver {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void onNewsPosted(BankNews news) {
        // Only notify customers for public news
        if (news.getIsPublic()) {
            List<User> customers = userRepository.findByRole_RoleName("Customer");
            
            for (User customer : customers) {
                Notification notification = new Notification();
                notification.setUserId(customer.getUserId());
                notification.setMessage("New bank news: " + news.getTitle());
                notification.setType("NEWS");
                notification.setIsRead(false);
                
                notificationRepository.save(notification);
            }
        }
    }
    
    @Override
    public String getObserverType() {
        return "CUSTOMER";
    }
}
