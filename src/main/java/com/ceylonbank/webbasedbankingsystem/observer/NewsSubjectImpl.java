package com.ceylonbank.webbasedbankingsystem.observer;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NewsSubjectImpl implements NewsSubject {
    
    private final List<NewsObserver> observers = new ArrayList<>();
    
    @Autowired
    public NewsSubjectImpl(List<NewsObserver> newsObservers) {
        this.observers.addAll(newsObservers);
    }
    
    @Override
    public void addObserver(NewsObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void removeObserver(NewsObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(BankNews news) {
        for (NewsObserver observer : observers) {
            try {
                observer.onNewsPosted(news);
            } catch (Exception e) {
                // Log error
                System.err.println("Error notifying observer " + observer.getObserverType() + ": " + e.getMessage());
            }
        }
    }
}
