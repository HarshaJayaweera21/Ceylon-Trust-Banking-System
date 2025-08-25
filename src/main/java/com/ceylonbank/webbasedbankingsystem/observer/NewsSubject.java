package com.ceylonbank.webbasedbankingsystem.observer;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;

public interface NewsSubject {
    void addObserver(NewsObserver observer);
    void removeObserver(NewsObserver observer);
    void notifyObservers(BankNews news);
}
